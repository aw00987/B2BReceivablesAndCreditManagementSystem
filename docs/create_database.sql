-- we don't know how to generate root <with-no-name> (class Root) :(

grant select on performance_schema.* to 'mysql.session'@localhost;

grant trigger on sys.* to 'mysql.sys'@localhost;

grant audit_abort_exempt, firewall_exempt, select, system_user on *.* to 'mysql.infoschema'@localhost;

grant audit_abort_exempt, authentication_policy_admin, backup_admin, clone_admin, connection_admin, firewall_exempt, persist_ro_variables_admin, session_variables_admin, shutdown, super, system_user, system_variables_admin on *.* to 'mysql.session'@localhost;

grant audit_abort_exempt, firewall_exempt, system_user on *.* to 'mysql.sys'@localhost;

grant alter, alter routine, application_password_admin, audit_abort_exempt, audit_admin, authentication_policy_admin, backup_admin, binlog_admin, binlog_encryption_admin, clone_admin, connection_admin, create, create role, create routine, create tablespace, create temporary tables, create user, create view, delete, drop, drop role, encryption_key_admin, event, execute, file, firewall_exempt, flush_optimizer_costs, flush_status, flush_tables, flush_user_resources, group_replication_admin, group_replication_stream, index, innodb_redo_log_archive, innodb_redo_log_enable, insert, lock tables, passwordless_user_admin, persist_ro_variables_admin, process, references, reload, replication client, replication slave, replication_applier, replication_slave_admin, resource_group_admin, resource_group_user, role_admin, select, sensitive_variables_observer, service_connection_admin, session_variables_admin, set_user_id, show databases, show view, show_routine, shutdown, super, system_user, system_variables_admin, table_encryption_admin, telemetry_log_admin, trigger, update, xa_recover_admin, grant option on *.* to root@localhost;

create table companies
(
    id            bigint auto_increment
        primary key,
    company_code  varchar(50)                        not null,
    company_name  varchar(255)                       not null,
    credit_rating varchar(10)                        null,
    credit_limit  decimal(18)                        null,
    pic_username  varchar(50)                        not null,
    address       varchar(255)                       not null,
    email         varchar(255)                       null,
    created_at    datetime default CURRENT_TIMESTAMP not null,
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    phone_num     varchar(50)                        null,
    fax_num       varchar(50)                        null,
    contract_id   varchar(100)                       null,
    constraint uk_company_code
        unique (company_code)
);

create table invoices
(
    id                  bigint auto_increment
        primary key,
    invoice_no          varchar(50)                                                 not null,
    company_code        varchar(50)                                                 not null,
    invoice_amount      decimal(18)                                                 not null,
    issue_date          date                                                        not null,
    due_date            date                                                        not null,
    created_by          varchar(50)                                                 not null,
    status              enum ('NORMAL', 'PAID', 'OVERDUE', 'DUNNING', 'LITIGATION') not null,
    principal_amount    decimal(18)                                                 not null,
    interest_amount     decimal(18)                                                 null,
    interest_start_date date                                                        null,
    notes               text                                                        null,
    created_at          datetime default CURRENT_TIMESTAMP                          not null,
    updated_at          datetime default CURRENT_TIMESTAMP                          not null on update CURRENT_TIMESTAMP,
    constraint invoices_invoice_no_uindex
        unique (invoice_no)
);

create table reconciliations
(
    id                    bigint auto_increment
        primary key,
    invoice_no            varchar(50)                                                 not null,
    bank_transaction_id   varchar(50)                                                 not null,
    payer_name            varchar(50)                                                 null,
    transaction_date      date                                                        null,
    transaction_amount    decimal(18)                                                 not null,
    reconciliation_amount decimal(18)                                                 null,
    match_type            enum ('EXACT', 'NAME_FUZZY', 'AMOUNT_VARIANCE', 'COMBINED') not null,
    description           text                                                        null,
    variance_amount       decimal(18)                                                 null,
    reconciled_at         datetime default CURRENT_TIMESTAMP                          not null,
    reconciled_by         varchar(50)                                                 null
);

create table users
(
    id            bigint auto_increment
        primary key,
    username      varchar(50)                                 not null,
    password_hash varchar(255)                                not null,
    real_name     varchar(100)                                not null,
    role          enum ('ADMIN', 'SALES', 'LEGAL', 'FINANCE') not null,
    status        tinyint  default 1                          not null,
    created_at    datetime default CURRENT_TIMESTAMP          not null,
    updated_at    datetime default CURRENT_TIMESTAMP          not null on update CURRENT_TIMESTAMP,
    constraint uk_username
        unique (username)
);

