package com.example.stores

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.Android
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class EditStoreFragment : Fragment() {

    private  lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if(id != null && id != 0L){
            mIsEditMode = true
            getStore(id)
        }
        else
        {
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)

        mBinding.etPhotoUrl.addTextChangedListener {
            Glide.with(this)
                .load(mBinding.etPhotoUrl.text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)
        }
    }

    private fun getStore(id: Long) {
        doAsync {
            mStoreEntity = StoreApplication.dataBase.storeDao().getStoreById(id)
            uiThread {
                if(mStoreEntity != null)
                    setUriStore(mStoreEntity!!)
            }
        }
    }

    private fun setUriStore(storeEntity: StoreEntity) {
        with(mBinding){
            etName.text = storeEntity.name.editable()
            etPhone.text = storeEntity.phone.editable()//setText(storeEntity.phone)
            etWebSite.text = storeEntity.webSite.editable()
            etPhotoUrl.text = storeEntity.photoUrl.editable()
        }
    }

    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save ->{
                if(mStoreEntity != null)
                {
                    /*val store = StoreEntity(
    name = mBinding.etName.text.toString().trim(),
    phone = mBinding.etPhone.text.toString().trim(),
    webSite = mBinding.etWebSite.text.toString().trim(),
    photoUrl = mBinding.etPhotoUrl.text.toString().trim()
)*/
                    with(mStoreEntity!!){
                        name = mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        webSite = mBinding.etWebSite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                    }
                    doAsync {
                        if(mIsEditMode)
                            StoreApplication.dataBase.storeDao().updateStore(mStoreEntity!!)
                        else
                            mStoreEntity?.id = StoreApplication.dataBase.storeDao().addStore(mStoreEntity!!)
                        uiThread {
                            hideKeyboard()
                            if(mIsEditMode)
                            {
                                mActivity?.updateStore(mStoreEntity!!)
                                Snackbar.make(
                                    mBinding.root,
                                    R.string.edit_store_message_update_success,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                            else {
                                mActivity?.addStore(mStoreEntity!!)
                                Toast.makeText(
                                    mActivity,
                                    R.string.edit_store_message_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                mActivity?.onBackPressed()
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFabe(true)
        setHasOptionsMenu(false)
        super.onDestroy()
    }

    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }
}