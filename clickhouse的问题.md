# Clickhouse问题

```text
sudo -u clickhouse clickhouse-server --config-file=/etc/clickhouse-server/config.xml    
 Application: DB::Exception: Listen [::]:8123 failed: Poco::Exception. Code: 1000, e.code() = 0, DNS error: EAI: Address family for hostname not supported (version 21.9.4.35 (official build))
```

```text
ping 2606:4700:4700::1111
ping: 2606:4700:4700::1111: Address family for hostname not supported
不支持ipv6

原因：
clickhouse支持多文件配置管理，config.d文件夹中的xml文件会覆盖主配置文件中的设置

config.d文件自带一个listen.xml，会将主配置文件的listen_listen_host覆盖为::

"::"是IPv6中的一个特殊地址,但不支持ipv6的系统就会出错

```
