-- Тестовые данные для демонстрации (выполнить после первого запуска программы).
INSERT INTO api_clients (full_name, email, company_name, client_type) VALUES
 ('Иванов Иван Иванович',    'ivanov@shopsoft.ru',    'ShopSoft',   'COMPANY'),
 ('Петрова Анна Сергеевна',  'petrova@payflow.ru',    'PayFlow',    'STARTUP'),
 ('Сидоров Алексей Олегович','sidorov@delivery24.ru', 'Delivery24', 'COMPANY'),
 ('Козлова Мария Андреевна', 'kozlova@analytix.ru',   'Analytix',   'INDIVIDUAL');

INSERT INTO api_connection_requests
 (client_id, title, description, api_type, priority, status, expected_daily_requests) VALUES
 (1, 'Выгрузка каталога товаров', 'Нужен доступ к API каталога для синхронизации товаров с нашей CRM', 'CATALOG', 'MEDIUM', 'NEW', 20000),
 (1, 'Приём заказов из CRM', 'Автоматическая передача заказов из CRM в маркетплейс через API', 'ORDERS', 'HIGH', 'IN_REVIEW', 50000),
 (2, 'Подключение платёжного API', 'Интеграция платёжного шлюза для приёма оплаты заказов', 'PAYMENTS', 'HIGH', 'APPROVED', 120000),
 (3, 'Трекинг доставки', 'Получение статусов доставки заказов для личного кабинета клиентов', 'DELIVERY', 'LOW', 'CONNECTED', 8000),
 (4, 'Аналитика продаж', 'Доступ к API аналитики для построения отчётов по продажам', 'ANALYTICS', 'MEDIUM', 'REJECTED', 3000),
 (4, 'Отзывы о товарах', 'Сбор отзывов о товарах для модерации во внутренней системе', 'REVIEWS', 'LOW', 'CANCELLED', 1500);

INSERT INTO api_catalog (name, description, version, api_type, status, rate_limit_per_minute, monthly_price) VALUES
 ('Каталог товаров', 'Чтение и выгрузка товаров, цен и остатков', '1.0', 'CATALOG',   'ACTIVE',     1200, 0),
 ('Заказы',          'Создание и отслеживание заказов маркетплейса', '2.1', 'ORDERS',   'ACTIVE',      600, 3000),
 ('Платежи',         'Приём платежей картой и через СБП',           '1.0', 'PAYMENTS',  'ACTIVE',      300, 5000),
 ('Доставка',        'Расчёт стоимости и статусы доставки',          '1.2', 'DELIVERY',  'DRAFT',       200, 1500),
 ('Аналитика',       'Отчёты по продажам и покупателям',             '0.9', 'ANALYTICS', 'DEPRECATED',  100, 2000);

INSERT INTO api_keys (key_value, client_id, request_id, status, issued_at, expires_at) VALUES
 ('mk_demo0000000000000000000000000001', 2, 3, 'ACTIVE',  CURRENT_TIMESTAMP,                       CURRENT_TIMESTAMP + INTERVAL '90 days'),
 ('mk_demo0000000000000000000000000002', 3, 4, 'ACTIVE',  CURRENT_TIMESTAMP,                       CURRENT_TIMESTAMP + INTERVAL '30 days'),
 ('mk_demo0000000000000000000000000003', 3, 4, 'REVOKED', CURRENT_TIMESTAMP - INTERVAL '40 days',  CURRENT_TIMESTAMP + INTERVAL '50 days');
