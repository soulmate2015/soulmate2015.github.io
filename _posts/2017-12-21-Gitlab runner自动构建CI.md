---
layout: post
title: Gitlab runner自动构建CI
author: Tower
date: '2017-12-21 00:00:00'
category: automation
summary: Gitlab runner构建
thumbnail: 171221runner.png
---

## 在了解GitLab-CI与GitLab Runner的使用之前，先提及一下持续集成概念
> 持续集成是一种软件开发实践，即团队开发成员经常集成他们的工作，通常每个成员每天至少集成一次，也就意味着每天可能会发生多次集成。每次集成都通过自动化的构建（包括编译，发布，自动化测试)来验证，从而尽快地发现集成错误。许多团队发现这个过程可以大大减少集成的问题，让团队能够更快的开发内聚的软件。

通俗一点来说，就是用自动化的方式代替开发工程中，除去编码部分，需要重复手工执行的环节。比如：合并代码---->安装依赖---->编译---->测试---->发布。持续集成可以节省人力成本，尽早发现软件集成过程中的错误，完成部署前的单元测试。

持续集成：
![deploy using travis](/assets/img/posts/171221runner1.jpg){:class="img-fluid"}

## 对GitLab-CI 和 GitLab-Runner的理解

* GitLab-CI: 是一套配合GitLab使用的持续集成系统，GitLab8.0以后的版本是默认集成了GitLab-CI并且默认启用

* GitLab-Runner: 是一个用来执行软件集成脚本的东东，需要配合GitLab-CI进行使用的。一般地，GitLab里面的每一个工程都会定义一个属于这个工程的软件集成脚本，用来自动化地完成一些软件集成工作。当这个工程的仓库代码发生变动时，比如有人push了代码，GitLab就会将这个变动通知GitLab-CI。这时GitLab-CI会找出与这个工程相关联的Runner，并通知这些Runner把代码更新到本地并执行预定义好的执行脚本

Runner可以分布在不同的主机上，同一个主机上也可以有多个Runner

## 下面介绍一下GitLab-Runner的安装与使用

### 1. 在需要部署代码的服务器安装runners：

*查看服务器对应runners的安装方式*

#### 查看linux服务器
```powershell
cat /proc/version
```

#### 下载runners Linux x86-64
```powershell
sudo wget -O /usr/local/bin/gitlab-runner https://gitlab-runner-downloads.s3.amazonaws.com/latest/binaries/gitlab-runner-linux-amd64
```

#### 赋予它执行权限
```powershell
sudo chmod +x /usr/local/bin/gitlab-runner
```

#### 创建GitLab CI用户：
```powershell
sudo useradd --comment 'GitLab Runner' --create-home gitlab-runner --shell /bin/bash
```

#### 查看$PATH
```powershell
echo $PATH
```

#### 解决root权限下没有命令
```powershell
echo "export PATH=$PATH:/usr/local/bin" >> /etc/profile
source /etc/profile
```

#### 安装并作为服务运行：
```powershell
sudo gitlab-runner install --user=gitlab-runner --working-directory=/home/gitlab-runner
sudo gitlab-runner start
```

参考链接：
> * [Install GitLab Runner manually on GNU/Linux](https://docs.gitlab.com/runner/install/linux-manually.html)


### 2. 在部署代码的服务器注册runners：

先在gitlab的CI/CD页面找到url和token，如图:
![deploy using travis](/assets/img/posts/171221runner2.png){:class="img-fluid"}

#### 注册runners
```powershell
gitlab-runner register \
  --non-interactive \
  --url "http://XX" \
  --registration-token "XX" \
  --executor "shell" \
  --docker-image maven:latest \
  --description "Auto_CI" \
  --tag-list "Auto_CI" \
  --run-untagged \
  --locked="false"
```

这里选择了在host中执行shell，比较简单，更主流的做法可以选择使用`docker服务`

### 3. 创建.gitlab-ci.yml文件

`.gitlab-ci.yml`文件配置CI对项目执行的操作，它告诉GitLab runner该做什么。它位于存储库的根目录中，你代码的每次提交，GitLab都会查找.gitlab-ci.yml这个文件，并根据这个文件的内容，在Runner上启动你提交的工作

下面是一个简单的yml文件配置：
```yml
cache:
  paths:
  - node_modules/

stages:
- env
- server
- deploy

env_job:
  stage: env
  before_script:
  - ifconfig
  - pwd
  - whoami
  script:
  - uname -a
  - export
  tags:
  - Auto_CI

server_job:
  stage: server
  before_script:
  - cd /home/gitlab-runner/XX/项目目录
  - pwd
  - echo $CI_BRANCH_SERVER_NAME
  script:
  - git checkout .
  - git fetch origin
  - git checkout $CI_BRANCH_SERVER_NAME
  - git pull origin $CI_BRANCH_SERVER_NAME
  - yarn install
  tags:
  - Auto_CI

deploy_job:
  stage: deploy
  before_script:
  - cd /home/gitlab-runner/XX/项目目录
  script:
  - yarn stop
  - yarn tsc
  - yarn start
  tags:
  - Auto_CI
```

上面的配置主要做了两件事`server_job`和`deploy_job`，实现了拉取最新代码，编译部署

可以在gitlab的Pipelines看到任务的完成情况，如图:
![deploy using travis](/assets/img/posts/171221runner3.jpg){:class="img-fluid"}

可以在gitlab的Pipelines中具体任务中，看到任务脚本的执行情况，如图
![deploy using travis](/assets/img/posts/171221runner4.jpg){:class="img-fluid"}

## 最后是一些其他的思考

这里介绍的只是持续集成一个雏形，在面对需要高可用自动化的团队，还有很长的路要走。比如集成jira任务的管理，部署通知的邮件，使用docker代替文件部署等等。
自动化替代手工重复操作，是一个不可逆的过程。一旦你尝到了解放双手的甜头，这条路你就会越走越远...

