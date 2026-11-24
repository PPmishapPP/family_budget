-- Локальные тестовые данные для разработки (Android, web).
-- Запуск: psql -U <user> -d family_budget_v3 -f deploy/dev-seed.sql
-- Логин: android / Пароль: android123 (только для dev-окружения!)

create extension if not exists pgcrypto;

insert into app_user (login, password, user_zone)
values ('android', crypt('android123', gen_salt('bf', 12)), 'Asia/Yekaterinburg')
on conflict (login) do nothing;

-- Привязываем тестового пользователя к существующему счёту с данными.
insert into user_account (user_id, account_id)
select u.id, a.id
from app_user u
         join account a on a.name = 'Безопасное место для денег'
where u.login = 'android'
  and not exists (
    select 1 from user_account ua where ua.user_id = u.id and ua.account_id = a.id
  );

-- Второй (пустой) счёт, чтобы проверить переключение счетов и очистку данных (B-06).
insert into user_account (user_id, account_id)
select u.id, a.id
from app_user u
         join account a on a.name = 'Приёмка Web'
where u.login = 'android'
  and not exists (
    select 1 from user_account ua where ua.user_id = u.id and ua.account_id = a.id
  );

-- Немного медиа для проверки экрана «Кино».
insert into media (name, status, type, account_id, rating, description)
select v.name, v.status, v.type, a.id, v.rating, v.description
from (values ('Дюна: Часть вторая', 'WATCHED', 'MOVIE', 9, 'Продолжение истории Пола Атрейдеса.'),
             ('Разделение', 'IN_PROGRESS', 'SERIES', 8, 'Сотрудники корпорации проходят процедуру разделения памяти.'),
             ('Медленные лошади', 'PLAN_TO_WATCH', 'SERIES', null, 'Шпионский сериал по роману Мика Херрона.'),
             ('Интерстеллар', 'WATCHED', 'MOVIE', 10, 'Экспедиция за пределы Солнечной системы.')) as v(name, status, type, rating, description)
         join account a on a.name = 'Безопасное место для денег'
where not exists (
    select 1 from media m where m.name = v.name and m.account_id = a.id
  );
