---
layout: page
title: general
permalink: /blog/categories/general
---

<h5> 所属分类: {{ page.title }} </h5>

<div class="card">
{% for post in site.categories.general %}
 <li class="category-posts"><span>{{ post.date | date: "%Y-%m-%d" }}</span> &nbsp; <a href="{{ post.url }}">{{ post.title }}</a></li>
{% endfor %}
</div>