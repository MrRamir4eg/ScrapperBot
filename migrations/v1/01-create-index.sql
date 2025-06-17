--liquibase formatted sql
--changeset RamirKashshapov:1
create index links_url_idx on links(url);

--changeset RamirKashshapov:2
create index chats_links_ids_idx on chats_links(chat_id, link_id);
