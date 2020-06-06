# blog with jekyll

## 功能点
1. 本地CMS管理：[Jekyll Admin](https://jekyll.github.io/jekyll-admin/)
2. 全文站点搜索：[Algolia](https://www.algolia.com/)

## 本地启动
进入到工作目录:
```shell
bundle exec jekyll server
```

## 使用docker部署
Building Image:
```shell
docker build -t my-jekyll-blog .
```

Running container :
```shell
docker run -d -p 4000:4000 -it --volume="$PWD:/srv/jekyll" --name "my_blog" my-jekyll-blog:latest jekyll serve --watch
```

## 遇到的一些问题

### 1. windows jekyll如何使用中文路径

#### 出现问题
最近在使用jekyll在本地预览自己写的博客无法正常打开，而提交到github上却可以正常解析。看了一下发现是文件写的博客有什么变化，原来是因为博客的markdown文件使用了中文文件名，jekyll无法正常解析出现乱码

#### 解决方法：
修改安装目录\Ruby22-x64\lib\ruby\2.6.0\webrick\httpservlet下的filehandler.rb文件

```ruby
path = req.path_info.dup.force_encoding(Encoding.find("filesystem"))
+ path.force_encoding("UTF-8") # 加入编码
if trailing_pathsep?(req.path_info)  
```

```ruby
break if base == "/"
+ base.force_encoding("UTF-8") #加入編碼
break unless File.directory?(File.expand_path(res.filename + base))  
```

修改完重新jekyll serve即可支持中文文件名