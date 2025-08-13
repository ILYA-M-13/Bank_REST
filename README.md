## ⚙️ Конфигурация

### Файл `.env` (обязательные переменные)
Создайте файл `.env` в корне проекта:

```bash
# База данных
DB_PORT=5432
DB_NAME=bankcards
DB_USER=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_256bit_secret_key_here_min_32_chars  # Пример: "my_very_secret_key_with_at_least_32_characters"
JWT_EXPIRATION_MS=86400000  # 24 часа в миллисекундах

# Шифрование
ENCRYPTION_KEY=your_32_byte_key_for_aes_256  # Пример: "this_is_a_32_byte_key_for_aes_256!!"
```

### Инициализация базы данных
При первом запуске база создается автоматически через Liquibase миграции.
Ручное создание не требуется.

## 🚀 Запуск проекта

1. Соберите проект: `mvn clean package`
2. Запустите: `docker-compose up --build`

## 👨‍💻 Доступ к системе

По умолчанию создается администратор:
- Логин: `admin1`
- Пароль: `admin1`