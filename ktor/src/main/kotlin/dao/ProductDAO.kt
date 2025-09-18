package com.gnomeshift.dao

import com.gnomeshift.entities.Product
import com.gnomeshift.entities.ProductEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object ProductDAO {
    fun create(name: String, price: Double): Result<Product> {
        return try {
            transaction {
                val newProductEntity = ProductEntity.new {
                    this.name = name
                    this.price = price
                }
                Result.Success(newProductEntity.toDto())
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getById(id: Int): Result<Product> {
        return try {
            transaction {
                val productEntity = ProductEntity.findById(id)

                if (productEntity != null) {
                    Result.Success(productEntity.toDto())
                }
                else {
                    Result.Error(Exception("Product not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getAll(): Result<List<Product>> {
        return try {
            transaction {
                val products = ProductEntity.all().toList().map { it.toDto() }
                Result.Success(products)
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun update(id: Int, newName: String, newPrice: Double): Result<Product> {
        return try {
            transaction {
                val productEntity = ProductEntity.findById(id)

                if (productEntity != null) {
                    productEntity.name = newName
                    productEntity.price = newPrice
                    Result.Success(productEntity.toDto())
                }
                else {
                    Result.Error(Exception("Product not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun delete(id: Int): Result<Unit> {
        return try {
            transaction {
                val productEntity = ProductEntity.findById(id)

                if (productEntity != null) {
                    productEntity.delete()
                    Result.Success(Unit)
                }
                else {
                    Result.Error(Exception("Product not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }
}