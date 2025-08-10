package com.example.workingbackground

import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.webkit.URLUtil

class MainActivity : AppCompatActivity() {

    // Сервис и флаг его состояния
    private var myService: MyBoundService? = null
    private var isBound = false

    // Тег для логов
    private val TAG = "MainActivity"

    // Код запроса разрешения на запись в хранилище
    private val PERMISSION_REQUEST_CODE = 100

    // Реализация интерфейса для подключения к сервису
    private val serviceConnection = object : ServiceConnection {
        // Вызывается при успешном подключении к сервису
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Приведение типа к нашему кастомному Binder'у
            val binder = service as MyBoundService.LocalBinder
            // Получение экземпляра сервиса
            myService = binder.getService()
            isBound = true
            Log.d(TAG, "Service connected")
        }

        // Вызывается при неожиданном отключении сервиса
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Установка макета активности
        setContentView(R.layout.activity_main)

        // Инициализация кнопок из макета
        val btnStart: Button = findViewById(R.id.btnStart)
        val btnStop: Button = findViewById(R.id.btnStop)
        val btnDownload: Button = findViewById(R.id.btnDownload)

        // Обработка нажатия кнопки "Start Service"
        btnStart.setOnClickListener {
            if (isBound) {
                // Если сервис уже привязан, вызываем его метод
                myService?.startService()
            } else {
                // Привязка к сервису, если еще не привязаны
                bindService(
                    Intent(this, MyBoundService::class.java),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE // Автоматическое создание сервиса при необходимости
                )
            }
        }

        // Обработка нажатия кнопки "Stop Service"
        btnStop.setOnClickListener {
            if (isBound) {
                // Вызов метода сервиса
                myService?.stopService()
                // Отвязка от сервиса
                unbindService(serviceConnection)
                isBound = false
            }
        }

        // Обработка нажатия кнопки "Download MP3"
        btnDownload.setOnClickListener {
            // Проверка разрешения перед загрузкой
            if (checkStoragePermission()) {
                startDownload()
            } else {
                requestStoragePermission()
            }
        }
    }

    // Проверка наличия разрешения на запись в хранилище
    private fun checkStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запрос разрешения на запись в хранилище
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    // Запуск загрузки MP3 файла
    private fun startDownload() {
        // URL файла для загрузки
        val url = "https://rus.hitmotop.com/get/music/20250519/Artur_Pirozhkov_-_Samo_Sobojj_79186666.mp3"
        // Генерация имени файла на основе URL
        val fileName = URLUtil.guessFileName(url, null, "audio/mpeg")

        // Создание запроса на загрузку
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("MP3 Download") // Заголовок уведомления
            .setDescription("Downloading $fileName") // Описание
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Видимость уведомления
            .setDestinationInExternalPublicDir( // Путь сохранения
                Environment.DIRECTORY_DOWNLOADS, // Папка Downloads
                fileName
            )
            .setAllowedOverMetered(true) // Разрешить загрузку по мобильным данным
            .setAllowedOverRoaming(true) // Разрешить загрузку в роуминге

        // Получение системного DownloadManager
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // Запуск загрузки
        downloadManager.enqueue(request)
        Log.d(TAG, "Download started: $fileName")
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Проверка кода запроса и результата
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение получено - запускаем загрузку
            startDownload()
        } else {
            // Разрешение не получено
            Log.w(TAG, "Storage permission denied")
        }
    }

    // Очистка ресурсов при уничтожении активности
    override fun onDestroy() {
        super.onDestroy()
        // Отвязка от сервиса при уничтожении активности
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}