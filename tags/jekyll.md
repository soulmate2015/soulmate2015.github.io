---
layout: page
title: Jekyll
permalink: /blog/tags/jekyll
---
 
<h5> Posts by Tag : {{ page.title }} </h5>

<div class="card">
{% for post in site.tags.jekyll %}
 <li class="category-posts"><span>{{ post.date | date: "%Y-%m-%d" }}</span> &nbsp; <a href="{{ post.url }}">{{ post.title }}</a></li>
{% endfor %}
</div>