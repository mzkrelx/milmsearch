Insert test data to PostgreSQL
-----------------------------------------
```shell
$ psql -U milmsearch milmsearch_dev -c 'COPY ml_proposal FROM stdin WITH CSV HEADER;' < test/data/ml-proposal1.csv
```

Insert test data to H2 (web console)
-----------------------------------------
```sql
INSERT INTO ML_PROPOSAL
    SELECT * FROM CSVREAD('test/data/ml-proposal1.csv');
```

