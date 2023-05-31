# Docker

## 新建容器

```shell
docker run(docker container run)
```
选项

- -i
    即使没有attached，也要保持 STDIN 打开状态(开启交互，但是没有终端能提供)
- -t
    分配一个伪TTY(分配终端，但是没有交互)
- -d
    在后台运行容器，并且打印容器id

选项组合

- 无选项
    显示容器的输出，运行完自动关闭
- -d
    不显示容器的输出，运行完自动关闭
- -it
    分配一个交互式终端，保持运行
- -itd
    分配一个交互式终端，但仍在后台运行，保持运行

不同的command(仅linux)

- init(/sbin/init)
    容器启动时像在传统的Linux发行版中那样运行所有服务和守护进程
- bash(/bin/bash)
    只需要运行一个或多个特定的命令

## 导入导出

- docker export(docker container export)
    将容器导出为容器快照文件
-  docker save
    将镜像导出为镜像库存储文件
- docker import
    导入容器快照文件为镜像
- docker load
    导入镜像库存储文件为镜像

## Dockerfile 语法

| 命令 | 描述 |
| --- | --- |
| ARG | 构建参数 |
| FROM | 基于哪个镜像来实现 |
| MAINTAINER | 指定 Dockerfile 的作者或维护者 |
| LABEL | 为镜像添加元数据 |
| SHELL | 指定shell |
| RUN | 执行的命令 |
| USER | 指定当前用户 |
| ADD | 添加宿主机文件到容器里，有需要解压的文件会自动解压 |
| COPY | 添加宿主机文件到容器里 |
| EXPOSE | 声明容器将监听的端口号，但并不自动映射到宿主机上 |
| WORKDIR | 工作目录 |
| ENV | 声明环境变量 |
| VOLUME | 定义匿名卷 |
| HEALTHCHECK | 健康检查 |
| ONBUILD | 只有当以当前镜像为基础镜像，去构建下一级镜像的时候才会被执行 |
| ENTRYPOINT | 与CMD功能相同，但需docker run 不会覆盖，如果需要覆盖可增加参数-entrypoint 来覆盖 |
| CMD | 容器启动后所执行的程序，如果执行docker run 后面跟启动命令会被覆盖掉 |

----
[Docker — 从入门到实践](https://yeasy.gitbook.io/docker_practice)  
[如何编写优雅的Dockerfile](https://zhuanlan.zhihu.com/p/79949030)  
[如何正确使用 docker run -i、 -t、-d 参数](https://jerrymei.cn/docker-run-interactive-tty-detach/)