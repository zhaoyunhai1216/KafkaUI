package application.utils

import java.util
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import java.util.{Arrays, Collection, Collections, Map, Properties}

import application.Async
import application.utils.KafkaUtils.topics
import org.apache.kafka.clients.admin._
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.{CommonClientConfigs, admin}
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.{KafkaFuture, Node, TopicPartition}
import org.slf4j.{Logger, LoggerFactory}

/**
 * @title: KafkaEnv
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/24 21:36
 */
object KafkaUtils {
  val logger: Logger = LoggerFactory.getLogger(KafkaUtils.getClass)
  val consumer_props = new Properties
  consumer_props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  consumer_props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  consumer_props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
  consumer_props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])
  consumer_props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])


  def getAdminClient(
    name: String,
    bootstrap: String)
  : admin.AdminClient = {
    val adminConfig = new Properties
    adminConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrap)
    val client = admin.AdminClient.create(adminConfig)
    logger.info(s"[$name] 与集群<${bootstrap}>建立连接完毕.")
    client
  }

  /**
   * 获取节点信息
   *
   * @param adminClient
   * @return
   */
  def brokers(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[util.Collection[Node]] = {
    Async.toJavaFuture(adminClient.describeCluster().nodes())
  }

  /**
   * 获取指定节点信息
   */
  def broker(
    name: String,
    adminClient: admin.AdminClient,
    node: Node)
  : CompletableFuture[Collection[ConfigEntry]] = {
    val res = new ConfigResource(ConfigResource.Type.BROKER, node.id().toString)
    val config = adminClient.describeConfigs(Collections.singleton(res))
    Async.toJavaFuture(config.all())
      .thenApplyAsync(x => x.get(res).entries())
  }

  /**
   * 获取节点信息
   *
   * @param adminClient
   * @return
   */
  def controller(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[Node] = {
    val config = adminClient.describeCluster()
    Async.toJavaFuture(config.controller())
  }

  /**
   * 获取消费组信息
   *
   * @param adminClient
   * @return
   */
  def topicNames(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[util.Set[String]] = {
    val future = adminClient.listTopics().names()
    Async.toJavaFuture(future)
  }

  /**
   * 获取主题信息
   *
   * @param adminClient
   * @return
   */
  def topics(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[Map[String, TopicDescription]] = {
    topicNames(name, adminClient)
      .thenApplyAsync(topics => adminClient.describeTopics(topics).all().get())
  }

  /**
   * 获取主题信息
   *
   * @param adminClient
   * @return
   */
  def topic(
    name: String,
    topic: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[TopicDescription] = {
    val f = adminClient.describeTopics(Arrays.asList(topic)).all()
    Async.toJavaFuture(f).thenApplyAsync(x => x.get(topic))
  }

  /**
   * 获取消费组信息
   *
   * @param adminClient
   * @return
   */
  def groupNames(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[util.Collection[ConsumerGroupListing]] = {
    val list = adminClient.listConsumerGroups()
    Async.toJavaFuture(list.all())
  }

  /**
   * 根据ConsumerGroupListing 请求详细信息
   *
   * @param adminClient
   * @param groups
   * @return
   */
  def groupConsumers(
    adminClient: admin.AdminClient,
    groups: util.Collection[ConsumerGroupListing])
  : util.Map[String, ConsumerGroupDescription] = {
    val ids = groups
      .stream()
      .map[String](x => x.groupId())
      .collect(Collectors.toList[String])
    adminClient.describeConsumerGroups(ids).all().get()
  }

  /**
   * 获取消费组信息
   *
   * @param adminClient
   * @return
   */
  def groupConsumers(
    name: String,
    adminClient: admin.AdminClient)
  : CompletableFuture[util.Map[String, ConsumerGroupDescription]] = {

    groupNames(name, adminClient)
      .thenApplyAsync(groups => groupConsumers(adminClient, groups))
  }

  /**
   * 获取消费组信息
   *
   * @param adminClient
   * @return
   */
  def groupMetadata(
    name: String,
    adminClient: admin.AdminClient,
    groupId: String)
  : CompletableFuture[ConsumerGroupDescription] = {
    val t = adminClient.describeConsumerGroups(Arrays.asList(groupId))
    Async.toJavaFuture(t.all()).thenApplyAsync(x => x.get(groupId))
  }

  /**
   * 获取消费组信息
   *
   * @param adminClient
   * @return
   */
  def groupOffset(
    name: String,
    adminClient: admin.AdminClient,
    groupId: String)
  : CompletableFuture[util.Map[TopicPartition, OffsetAndMetadata]] = {
    val t = adminClient.listConsumerGroupOffsets(groupId)
    Async.toJavaFuture(t.partitionsToOffsetAndMetadata())
  }

  /**
   * 获取基本配置信息
   *
   * @return
   */
  def getConsimerProperties(): Properties = {
    val props = new Properties
    props.clone()
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])
    props
  }

  /**
   * 获取消费者
   *
   * @param bootstrap
   * @param groupId
   * @return
   */
  def getConsumer(
    bootstrap: String,
    groupId: String) = {
    val props = new Properties()
    props.putAll(consumer_props)
    props.put("bootstrap.servers", bootstrap)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put("auto.offset.reset", "latest")
    new KafkaConsumer[Array[Byte], Array[Byte]](props)
  }

  /**
   * 获取消费者
   *
   * @param bootstrap
   * @return
   */
  def getProducer(
    bootstrap: String) = {
    val props = new Properties
    props.put("bootstrap.servers", bootstrap)
    props.put("compression.type", "none")
    props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    new KafkaProducer[Array[Byte], Array[Byte]](props)
  }

  /**
   * 获取主题信息
   *
   * @param adminClient
   * @return
   */
  def createTopic(
    name: String,
    topic: String,
    partitions: Int,
    replica: Short,
    adminClient: admin.AdminClient)
  : CreateTopicsResult = {
    val newTopic = new NewTopic(topic, partitions, replica)
    adminClient.createTopics(util.Arrays.asList(newTopic))
  }


  /**
   * 获取主题信息
   *
   * @param adminClient
   * @return
   */
  def deleteTopic(
    name: String,
    topic: String,
    adminClient: admin.AdminClient)
  : DeleteTopicsResult = {
    adminClient.deleteTopics(util.Arrays.asList(topic))
  }
}
