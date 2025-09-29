package com.gnomeshift.dao

import com.gnomeshift.db.db
import com.gnomeshift.dto.ProductRequest
import com.gnomeshift.entities.Product
import com.gnomeshift.entities.ProductEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object ProductDAO {
    fun create(request: ProductRequest): Result<Product> {
        return try {
            transaction(db) {
                val newProductEntity = ProductEntity.new {
                    this.name = request.name
                    this.price = request.price
                }
                Result.Success(newProductEntity.toDto())
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun getById(id: Int): Result<Product> {
        return try {
            transaction(db) {
                ProductEntity.findById(id)?.toDto()?.let { Result.Success(it) }
                    ?: Result.Error(Exception("Product with id $id not found"))
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun getAll(): Result<List<Product>> {
        return try {
            transaction(db) {
                Result.Success(ProductEntity.all().toList().map { it.toDto() })
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun update(id: Int, request: ProductRequest): Result<Product> {
        return try {
            transaction(db) {
                val productEntity = ProductEntity.findById(id) ?: return@transaction Result.Error(Exception("Product with id $id not found."))
                productEntity.name = request.name
                productEntity.price = request.price

                Result.Success(productEntity.toDto())
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun delete(id: Int): Result<Unit> {
        return try {
            transaction(db) {
                val productEntity = ProductEntity.findById(id) ?: return@transaction Result.Error(Exception("Product with id $id not found."))
                productEntity.delete()

                Result.Success(Unit)
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }
}