package com.example.stores

interface MainAux {
    fun hideFabe(isVisible: Boolean = false)
    fun addStore(storeEntity: StoreEntity)
    fun updateStore(storeEntity: StoreEntity)
}