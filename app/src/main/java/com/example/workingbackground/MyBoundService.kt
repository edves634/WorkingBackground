package com.example.workingbackground

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

// Класс Bound Service, который позволяет компонентам привязываться к нему
class MyBoundService : Service() {

    // Binder для предоставления интерфейса сервиса клиентам
    private val binder = LocalBinder()

    // Тег для логов
    private val TAG = "MyBoundService"

    // Внутренний класс Binder, который возвращает экземпляр сервиса
    inner class LocalBinder : Binder() {
        // Метод для получения ссылки на текущий сервис
        fun getService(): MyBoundService = this@MyBoundService
    }

    // Вызывается, когда компонент (например, Activity) хочет привязаться к сервису
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service bound") // Логирование события привязки
        return binder // Возвращаем IBinder, через который клиент будет взаимодействовать с сервисом
    }

    // Вызывается, когда все клиенты отвязались от сервиса
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound") // Логирование события отвязки
        return super.onUnbind(intent)

    }

    // Кастомный метод сервиса, который может вызываться из Activity
    fun startService() {
        Log.d(TAG, "Service started") // Логирование "запуска" сервиса

    }

    // Кастомный метод сервиса, который может вызываться из Activity
    fun stopService() {
        Log.d(TAG, "Service stopped") // Логирование "остановки" сервиса

    }


}