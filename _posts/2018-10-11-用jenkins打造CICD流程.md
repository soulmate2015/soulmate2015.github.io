---
layout: post
title: 利用jenkins打造CI/CD流程
author: Tower
date: '2018-10-11 00:00:00'
category: automation
summary: jenkins构建
thumbnail: 181011jenkins.png
---

## 初衷

随着业务趋于平缓，提升团队开发效率，用自动化代替重复人工操作的事宜被提上了日程。
之前也有做一些CI/CD的解决方案，但没能完全解决日常开发中的痛点。发包，发邮件，部署环境等还是占用了我们很大一部分时间。

项目之前用的是gitlab自带的Runner做持续集成。配置.gitlab-ci.yml后，提交到指定分支时，触发CI pipeline。完成后续编译，部署等一系列动作。做为个人环境部署，或者不太规范的小团队使用还可以。人员和环节一旦多起来，就显的力不从心了。

在衡量我们自身需求之后，选择了业内成熟的解决方案Jenkins做为替代方案。

## 利用jenkins打造CI/CD流程

