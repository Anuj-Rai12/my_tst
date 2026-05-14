package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.pos10.db.entity.WorkOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {

    // Insert or update work orders
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrders(orders: List<WorkOrderEntity>)

    // Insert single work order
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(order: WorkOrderEntity)

    // Get all work orders
    @Query("SELECT * FROM work_orders")
    fun getAllWorkOrders(): Flow<List<WorkOrderEntity>>

    // Get work order by workid
   /* @Query("SELECT * FROM work_orders WHERE workid = :workid")
    suspend fun getWorkOrderById(workid: Int): WorkOrderEntity?*/

    // Delete a work order by id
    @Query("DELETE FROM work_orders WHERE workid = :workid")
    suspend fun deleteWorkOrder(workid: Int)

    // Delete all work orders
    @Query("DELETE FROM work_orders")
    suspend fun deleteAllWorkOrders()

    // Update work order
    @Update
    suspend fun updateWorkOrder(order: WorkOrderEntity)

    @Query("""
    UPDATE work_orders 
    SET 
        workStatus = :status, 
        workStatusid = :workStatusid,
        appointmentDate = COALESCE(:appointmentDate, appointmentDate), 
        appointmentTime = COALESCE(:appointmentTime, appointmentTime) 
    WHERE workid = :workId
""")
    suspend fun updateWorkOrderStatus(
        workId: Int,
        status: Int,
        workStatusid:String,
        appointmentDate: String? = null,
        appointmentTime: String? = null
    )

    @Query("SELECT * FROM work_orders WHERE workStatus = :status")
    fun getWorkOrdersByStatus(status: String): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE workid = :id LIMIT 1")
    suspend fun getWorkOrderById(id: Int): WorkOrderEntity?

    @Query("SELECT * FROM work_orders")
    fun getAllWorkOrdersFlow(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE workid = :workOrderId")
    fun getWorkOrderFlow(workOrderId: Int): Flow<WorkOrderEntity?>


    @Query("DELETE FROM work_orders WHERE workid NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Int>)

    @RawQuery
    suspend fun updateDynamic(query: SupportSQLiteQuery): Int

}


