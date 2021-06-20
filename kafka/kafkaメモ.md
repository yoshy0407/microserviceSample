## トピックの作成

### customer
customerのvalidation受付トピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic customer-validation'
```

customerのvalidation戻りトピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic customer-validation-result'
```

customerのvalidationロールバック用
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic customer-validation-rollback'
```

customerのvalidation完了処理用
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic customer-validation-complete'
```
### book

bookのreduceStock受付トピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic book-stock'
```

bookのreduceStock戻りトピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic book-stock-result'
```

bookのreduceStockロールバックトピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic book-stock-rollback'
```

bookのreduceStock完了トピック
```
docker exec -it ms-kafka bash -c 'kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic book-stock-complete'
```


## 管理系
topicへのメッセージ送信
```
docker exec -it ms-kafka bash -c 'kafka-console-producer.sh --bootstrap-server=localhost:9092 --topic=customer-validation'
```

topicの中身確認
```
docker exec -it ms-kafka bash -c 'kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic customer-validation --from-beginning'
```

topicの中のメッセージの削除
```
docker exec -it ms-kafka bash -c 'kafka-configs.sh --zookeeper zookeeper:2181 --alter --entity-type topics --add-config retention.ms=1000 --entity-name customer-validation'

docker exec -it ms-kafka bash -c 'kafka-configs.sh --zookeeper zookeeper:2181 --entity-type topics --describe --entity-name customer-validation'
```