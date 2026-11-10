# Лабораторная работа № 8 — Погода

Android-приложение на Kotlin. Текущая погода и прогноз на пять дней для Новосибирска, Москвы и Санкт-Петербурга загружаются из Open-Meteo без API-ключа.

В приложении есть выбор города, обновление прогноза, индикатор загрузки и сообщения об ошибках. Коды WMO преобразуются в описания и значки в `WeatherMapper`. Неизвестный код отображается как «Неизвестно». Состояние экрана хранится в ViewModel и сохраняется при повороте.

## Структура

`WeatherApiService` → `WeatherRepository` → `WeatherViewModel` → `MainActivity`. DTO ответа и модели экрана разделены; `WeatherMapper` обрабатывает данные до отображения.

## Запуск

Требуются JDK 21, Android SDK 35 и Android 7.0 или новее. Открыть проект в Android Studio либо выполнить:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Установочный файл: [app-debug.apk](app-debug.apk). Для получения прогноза нужен интернет.

Документация сервиса: [Open-Meteo](https://open-meteo.com/en/docs).
