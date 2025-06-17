--liquibase formatted sql
--changeset RamirKashshapov:1
create table if not exists chats(
    id bigint primary key
);

--changeset RamirKashshapov:2
create table if not exists links(
    id bigserial primary key,
    url text not null unique,
    last_updated timestamp with time zone not null,
    last_checked timestamp with time zone not null
);

--changeset RamirKashshapov:3
create table if not exists chats_links(
    chat_id bigint references chats(id),
    link_id bigint references links(id),
    tags text,
    filters text
);



