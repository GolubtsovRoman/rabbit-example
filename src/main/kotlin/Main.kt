package ru.golubtsov

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.Queue
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.lang.Thread.sleep
import kotlin.text.Charsets.UTF_8


fun main() {
    println("=== START ===")

    // create connect
    val rabbitConnection = ConnectionFactory().apply {
        host = "rabbit"
        port = 5672
        username = "guest"
        password = "guest"
    }
        .newConnection()

    // check connect is ok
    println("Connect is open: ${rabbitConnection.isOpen}")

    // declare queue
    val queueName = "example-queue"
    val channel = rabbitConnection.createChannel()
    channel.queueDeclare(queueName, true, false, false, null)

    // push message
    val messageOne = "message one"
    channel.basicPublish("", queueName, null, messageOne.toByteArray())
    val messageTwo = "message two"
    channel.basicPublish("", queueName, null, messageTwo.toByteArray())

    sleep(10) // for publishing

    // check count of message
    println("Message count is 2: ${channel.messageCount(queueName) == 2L}")

    // listen message
    // other thread
    val consumer: DefaultConsumer = object : DefaultConsumer(channel) {
        override fun handleDelivery(
            consumerTag: String,
            envelope: Envelope,
            properties: AMQP.BasicProperties,
            body: ByteArray
        ) {
            val message = String(body, UTF_8)
            println("Catch message: $message")
        }
    }
    channel.basicConsume(queueName, true, consumer)
    channel.basicConsume(queueName, true, consumer)


    // check count of message
    println("Message count is 0: ${channel.messageCount(queueName) == 0L}")

    // delete queue
    channel.queueDelete(queueName)

    // close all resource
    channel.close()
    rabbitConnection.close()

    // check connect
    println("Connect is open: ${rabbitConnection.isOpen}")
    println("=== END ===")
}
