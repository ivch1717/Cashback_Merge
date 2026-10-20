# Cashback Merge

Мини-игра по правилам 2048: одинаковые плитки кэшбэка объединяются,
например `1% + 1% = 2%`. Программа создаёт партии по seed, автоматически
играет выбранной стратегией, рассчитывает награды и формирует отчёты.

Реализованы МР1–МР3 и все шесть бонусных возможностей из задания.

## Требования и быстрый запуск

Нужен JDK 25 или новее. Gradle отдельно устанавливать не нужно.
Все команды ниже выполняются в PowerShell из корня проекта.
Java должна быть доступна через `JAVA_HOME` или `PATH`.

```powershell
java -version
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 10000 --strategy random --seed 42 --output report.html"
```

Команда играет 10000 партий и записывает HTML-отчёт в `report.html`.
Открой файл в браузере. Отчёты не требуют сервера или интернета.
Относительные пути считаются от рабочей папки программы.
Файл по указанному пути перезаписывается; родительская папка должна существовать.

Для Linux/macOS вместо `.\gradlew.bat` используй `./gradlew`.

## Правила игры и принятые решения

- Размер поля задаётся `boardSize`: квадратное поле от `2×2`. По умолчанию в примерах — `4×4`.
- В начале появляются две плитки. Один конфиг и seed дают одинаковое начальное поле.
- Ходы: `UP`, `DOWN`, `LEFT`, `RIGHT`. Одинаковые плитки сливаются со стороны движения.
- Одна плитка участвует максимум в одном слиянии за ход:
  `[1,1,2,0] → LEFT → [2,2,0,0]`.
- Ход без изменений отклоняется исключением, не расходует лимит и не создаёт плитку.
- После успешного хода появляется ровно одна новая плитка и расходуется один ход.
- Score начинается с нуля. Каждое слияние добавляет значение получившейся плитки:
  `2 + 2 → 4` даёт 4 очка. За сдвиг и появление плитки очки не начисляются.
- Достижение `32%` не завершает игру. Плитки могут объединяться дальше.
- Автоматический игрок заканчивает партию при исчерпании лимита или отсутствии допустимых ходов.
- Вероятности и награды хранятся в `BigDecimal`. Появление плиток выбирается по целым весам без `double`.
- Награда выбирается по самому высокому уровню `rewardTable`, не превышающему максимальную плитку.
  Для примера конфига: `1% → 0`, `16% → 500`, `64% → 2000`.
- В текущей проверке ограничения учитываются партии, чей выбранный уровень награды
  не ниже `maxRewardCashback`. В приложенных кампаниях это уровень максимальной награды `32%`.

Ручная игра с клавиатуры не реализована: основной сценарий — автоматическая симуляция.

## Конфиги кампаний

| Файл | Лимит ходов | Назначение |
|---|---:|---|
| `config/campaign.example.json` | 100 | Исходный пример задания |
| `config/campaign.conservative.json` | 32 | Более строгая кампания |
| `config/campaign.balanced.json` | 36 | Пример баланса с результатом `OK` для обеих стратегий |
| `config/campaign.generous.json` | 60 | Более щедрая кампания для сравнения |

Все примеры используют вероятности `0.85` для `1%` и `0.15` для `2%`,
одинаковую таблицу наград и допустимую долю `0.20`.

Поля конфига:

| Поле | Значение |
|---|---|
| `boardSize` | Длина стороны поля, целое число больше 1 |
| `movesLimit` | Положительный лимит успешных ходов |
| `spawnRules` | Значения новых плиток и вероятности их появления; сумма вероятностей ровно 1 |
| `rewardTable` | Уровни плиток и соответствующие награды |
| `maxRewardCashback` | Уровень, достижение которого учитывается при проверке ограничения |
| `maxRewardAllowedRate` | Допустимая доля таких игроков: `0.20` означает 20% |

## Стартовое поле

Без подкоманды программа только создаёт и печатает начальное поле:

```powershell
.\gradlew.bat run
.\gradlew.bat run --args="42 config/campaign.example.json"
.\gradlew.bat run --args="123 config/campaign.balanced.json"
```

Позиционные аргументы — seed и путь к конфигу.
Без аргументов используются `42` и `config/campaign.example.json`.

## Симуляция одной кампании

JSON-отчёт для случайной стратегии:

```powershell
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 10000 --strategy random --seed 42 --output report.json"
```

HTML-отчёт для той же кампании:

```powershell
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 10000 --strategy random --seed 42 --output report.html"
```

Симуляция со стратегией `greedy-score`:

```powershell
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 10000 --strategy greedy-score --seed 42 --output report-greedy.html"
```

Короткий запуск с параметрами по умолчанию:

```powershell
.\gradlew.bat run --args="simulate"
```

Он использует исходный `campaign.example.json` и сохраняет `report.json`.
Для этого конфига статус `OK` не гарантируется.

Параметры `simulate`:

| Параметр | По умолчанию | Назначение |
|---|---|---|
| `--config` | `config/campaign.example.json` | Конфиг одной кампании |
| `--players` | `10000` | Число партий, больше 0 |
| `--strategy` | `random` | `random` или `greedy-score` |
| `--seed` | `42` | Общий seed симуляции |
| `--output` | `report.json` | Путь к отчёту |
| `--replay-player` | `-1` | Номер сохраняемого игрока с 1; `-1` отключает запись |

Отчёт содержит число игроков, лимит ходов, seed, стратегию,
распределения максимальных плиток и наград, средний и максимальный score,
долю максимальной награды и статус `OK` / `FAILED`.

Статус `FAILED` означает превышение ограничения кампании, а не ошибку запуска.
Сравнение с ограничением выполняется без округления. Доля в отчёте округляется до 10 знаков.

## Бонусы

### 1. Стратегия greedy-score — 5 баллов

Игрок рассматривает доступные направления и выбирает ход с максимальным приростом score
за один ход. Например, между ходами с приростом 4 и 12 выбирается второй.
При равенстве выигрывает первый ход из списка доступных направлений.

Оценка выполняется на отдельном временном поле: настоящие плитки, score,
счётчик ходов и генератор появления плиток не изменяются.
Новая плитка появляется только после выполнения выбранного хода.
Алгоритм не заглядывает на несколько ходов вперёд.

Запуск: добавь `--strategy greedy-score`, как в примере симуляции выше.

### 2. Property-based проверки merge-логики — 5 баллов

Файл: `src/test/java/academy/cashbackmerge/bonus/MergePropertiesTest.java`.
Реализованы генеративные проверки на JUnit без дополнительных библиотек:

- 1000 случайных полей для каждого из четырёх направлений, размеры от `2×2` до `8×8`;
- полный перебор 256 строк из значений `0`, `1`, `2`, `4` для каждого направления;
- 100 случайных партий по 50 попыток хода для проверки накопления score.

Всего проверяется 10024 попытки хода.
Результат сравнивается с независимым эталонным алгоритмом:
убрать нули, объединить одинаковые пары и дополнить строку нулями.
Дополнительно проверяются сохранение суммы, отсутствие промежутков со стороны движения,
невозрастание числа плиток, корректность score и отсутствие изменений при `canMove()`.

Генерация воспроизводима по seed 42. Ошибка содержит исходное поле, направление и номер попытки.
Автоматическое уменьшение ошибочного примера не реализовано.

```powershell
.\gradlew.bat test --tests "academy.cashbackmerge.bonus.MergePropertiesTest"
```

### 3. Сравнение нескольких кампаний — 5 баллов

Метод `Simulator.compareCampaigns()` загружает несколько конфигов и запускает каждый
с одинаковыми числом игроков, seed и стратегией. Результаты собираются по именам конфигов.

Сравнение трёх подготовленных кампаний:

```powershell
.\gradlew.bat run --args="compare --players 10000 --strategy random --seed 42 --output comparison-random.html"
.\gradlew.bat run --args="compare --players 10000 --strategy greedy-score --seed 42 --output comparison-greedy.html"
```

Свой список конфигов передаётся через запятую, без пробелов:

```powershell
.\gradlew.bat run --args="compare --configs config/campaign.balanced.json,config/campaign.generous.json --players 10000 --strategy greedy-score --seed 42 --output comparison.json"
```

Без `--configs` используются `campaign.conservative.json`, `campaign.balanced.json`
и `campaign.generous.json` из папки `config/`.
Остальные параметры и их значения по умолчанию совпадают с симуляцией.
`--config` предназначен для `simulate`, `--configs` — для `compare`.
Параметр записи replay применяется только в `simulate`.

Результаты для 10000 игроков и seed 42:

| Кампания | Ходов | Random: доля / статус | Greedy-score: доля / статус |
|---|---:|---|---|
| conservative | 32 | 0,21% / OK | 2,90% / OK |
| balanced | 36 | 2,31% / OK | 15,57% / OK |
| generous | 60 | 75,32% / FAILED | 92,70% / FAILED |

Ограничение во всех трёх кампаниях — 20%.
Это эмпирические результаты указанных запусков, а не гарантия для любого seed или стратегии.
Готовые отчёты: [random](reports/comparison-random.html), [greedy-score](reports/comparison-greedy.html).

### 4. Размер поля через конфиг — 4 балла

Вместо фиксированного `4×4` используется значение `boardSize`.
Сдвиги, слияния, выбор свободных клеток и вывод поля работают с этим размером.
Минимальный размер — `2×2`, чтобы на поле помещались две стартовые плитки.

Для своего примера скопируй конфиг:

```powershell
Copy-Item config/campaign.balanced.json config/campaign.5x5.json
```

Измени в копии `"boardSize": 4` на `"boardSize": 5`, затем запусти:

```powershell
.\gradlew.bat run --args="42 config/campaign.5x5.json"
.\gradlew.bat run --args="simulate --config config/campaign.5x5.json --players 10000 --strategy random --seed 42 --output report-5x5.html"
```

### 5. HTML-отчёт — 3 балла

Расширение выходного файла определяет формат:

- `.html` — HTML, который можно открыть в браузере;
- остальные расширения — JSON. Для JSON рекомендуется `.json`.

Одиночный HTML-отчёт содержит параметры, score, статус и таблицы распределений.
HTML-сравнение показывает общую таблицу кампаний.
JSON-сравнение сохраняет полную статистику каждой кампании.
Файлы записываются в UTF-8. CSS, JavaScript и сервер не требуются.

```powershell
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 10000 --strategy greedy-score --seed 42 --output report.html"
.\gradlew.bat run --args="compare --players 10000 --strategy greedy-score --seed 42 --output comparison.html"
```

### 6. Replay партии по seed и списку ходов — 3 балла

При симуляции можно сохранить одну выбранную партию:

```powershell
.\gradlew.bat run --args="simulate --config config/campaign.balanced.json --players 1000 --strategy random --seed 42 --output report.json --replay-player 5"
```

Сохраняется пятый игрок. Нумерация с 1 до `--players`.
Без `--replay-player`, либо при значении `-1`, файл replay не записывается.

Файл `replay.json` создаётся в рабочей папке и содержит:

- полный конфиг кампании;
- seed именно выбранной партии, а не общий seed симуляции;
- последовательность успешно выполненных ходов.

Существующий `replay.json` перезаписывается при следующей записи.
При сохранённой партии изменение исходного JSON-конфига не влияет на replay,
поскольку конфиг уже находится в файле записи.

Воспроизведение:

```powershell
.\gradlew.bat run --args="replay --input replay.json"
```

Короткая команда читает `replay.json` по умолчанию:

```powershell
.\gradlew.bat run --args="replay"
```

Выводится начальное поле, затем поле после каждого сохранённого хода.
Стратегия заново ходы не выбирает. Ручная игра не нужна.
Одинаковые seed, конфиг и ходы воспроизводят те же поля и score в текущей реализации.

## Тесты и проверка кода

Все тесты:

```powershell
.\gradlew.bat test
```

Обязательные тесты по этапам:

```powershell
.\gradlew.bat test --tests "academy.cashbackmerge.mandatory.mr1.*"
.\gradlew.bat test --tests "academy.cashbackmerge.mandatory.mr2.*"
.\gradlew.bat test --tests "academy.cashbackmerge.mandatory.mr3.*"
```


Только бонусные генеративные тесты:

```powershell
.\gradlew.bat test --tests "academy.cashbackmerge.bonus.MergePropertiesTest"
```

Форматирование, тесты и линтеры:

```powershell
.\gradlew.bat spotlessApply check
```

Полная проверка, как в CI:

```powershell
.\gradlew.bat --continue assemble check printCoverage
```

Последняя проверенная версия: 33 тестовых запуска, все проходят, пропущенных нет.
Spotless проверяет форматирование, Checkstyle — структуру и стиль,
Error Prone и NullAway — ошибки кода и работу с `null`.
Отчёт покрытия: `build/reports/jacoco/test/html/index.html`.

## Кодировка терминала Windows

Если вместо рамки или русского текста выводятся некорректные символы, перед запуском выполни:

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
.\gradlew.bat run --args="replay --input replay.json"
```
