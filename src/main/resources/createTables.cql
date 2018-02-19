CREATE TABLE if not exists products (
    itemid text,
    version int,
    productid uuid,
    scopes map<text, text>,
    type text,
    attributes text,
    category text,
    PRIMARY KEY ((itemid, version), type, productid, category)
);

CREATE TABLE if not exists product_by_id (
    itemid text,
    version int,
    productid uuid,
    scopes map<text, text>,
    type text,
    attributes text,
    category text,
    PRIMARY KEY ((productid, itemid), version)
);

