# naive-distributed-cache-system
[naive-distributed-cache-system](https://github.com/XianReallyHot-ZZH/naive-distributed-cache-system)是一个naive级别的demo，架构设计来自ignite的官方live code session。
本demo旨在帮助理解分布式缓存系统的设计，同时帮助接触、理解ignite中相关的分布式功能实现上对应的概念和设计理念，方便后续自己项目yyignite的仿写与实现（仿写Apache-Ignite，理解梳理源码）。

## 使用方式
本项目依赖etcd实现分布式discovery机制，完成集群节点发现和topology的维护。
1. 开启本项目前请在本地开启etcd
2. 启动server node（可随时启动多个），server node间会自动相互发现进而组成集群。
3. 启动client node，client node不会加入集群，但是能感知集群的变化，在client端对缓存的操作，最终都会通过底层的集群感知和通信能力，将操作发送到对应的节点上执行。

## 简介
* 集群能力：集群节点发现、注册、topology维护、rebalance；
* 客户端能力：集群topology感知、分区映射计算；
* 通用能力：request事件网络通信；

## 主要核心概念和对应类说明
* Discovery：基于etcd实现discovery机制；
* Mapper：分区映射计算；
* Storage：分布式缓存管理；
* Communication：分布式缓存操作事件通信；
* Request：分布式缓存操作事件；
* Client：客户端；
* Server：服务端；

## 记下来的计划
接下来的计划会在[yyignite](https://github.com/XianReallyHot-ZZH/yyignite)中体现，将会逐步完成对Apache-Ignite项目的拆解、仿写、复现；
