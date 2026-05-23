create table companies
(
    id            bigint auto_increment primary key,
    company_code  varchar(50)                        not null,
    company_name  varchar(255)                       not null,
    address       varchar(255)                       not null,
    email         varchar(255)                       null,
    phone_num     varchar(50)                        null,
    fax_num       varchar(50)                        null,
    credit_rating varchar(10)                        null,
    credit_limit  decimal(18, 2)                     null,
    pic_user_id   bigint                        not null,
    created_at    datetime default CURRENT_TIMESTAMP not null,
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_company_code unique (company_code)
);

create table invoices
(
    id                  bigint auto_increment primary key,
    invoice_no          varchar(50)                                                 not null,
    company_id          bigint                                                      not null,# todo:询问数据库外键用法
    status              varchar(20)                                                 not null,
    invoice_amount      decimal(18, 2)                                              not null,
    principal_amount    decimal(18, 2)                                              not null,
    interest_amount     decimal(18, 2)                                              null,
    interest_start_date date                                                        null,
    issue_date          date                                                        not null,
    due_date            date                                                        not null,
    notes               text                                                        null,
    created_by_user_id  bigint                                                      not null,
    created_at          datetime default CURRENT_TIMESTAMP                          not null,
    updated_at          datetime default CURRENT_TIMESTAMP                          not null on update CURRENT_TIMESTAMP,
    constraint uk_invoice_no unique (invoice_no)
);

create table reconciliations
(
    id                    bigint auto_increment primary key,
    invoice_no            varchar(50)                                                           not null,
    bank_transaction_id   varchar(50)                                                           not null,
    payer_name            varchar(50)                                                           null,
    transaction_date      date                                                                  null,
    transaction_amount    decimal(18, 2)                                                        not null,
    reconciliation_amount decimal(18, 2)                                                        null,
    match_type            varchar(20)                                                           not null,
    description           text                                                                  null,
    variance_amount       decimal(18, 2)                                                        null,
    reconciled_at         datetime default CURRENT_TIMESTAMP                                    not null,
    reconciled_by         varchar(50)                                                           null
);

create table users
(
    id            bigint auto_increment primary key,
    username      varchar(50)                                            not null,
    password_hash varchar(255)                                           not null,
    real_name     varchar(50)                                           not null,
    role          varchar(20)                                            not null,
    status        varchar(20)                  default 'ENABLED'         not null,
    created_at    datetime                     default CURRENT_TIMESTAMP not null,
    updated_at    datetime                     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_username unique (username)
);
