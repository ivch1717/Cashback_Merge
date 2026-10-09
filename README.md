# Cashback Merge

Мини-игра в духе 2048 с плитками кэшбэка на поле 4×4.

В МР1 реализованы загрузка и проверка JSON-конфига, состояние игры,
создание двух стартовых плиток по seed и текстовый вывод поля.
Вероятности хранятся в `BigDecimal`. Одинаковый конфиг и seed дают одинаковое стартовое поле.
Ходы, слияния и симуляция появятся в следующих МР.

## Запуск

Нужен JDK 25 или новее. Команды для Windows:

```powershell
.\gradlew.bat run
.\gradlew.bat run --args="42 config/campaign.example.json"
```

Аргументы: seed и путь к конфигу. По умолчанию используются `42` и `config/campaign.example.json`.

## Проверки

```powershell
.\gradlew.bat test                 # тесты
.\gradlew.bat spotlessApply check  # форматирование, тесты и линтеры
```

Полное задание — в [TASK.md](TASK.md), помощь с настройкой — в [HELP.md](HELP.md).
