<#--
	前端文件目录 以及 版本号
-->
<#assign contextPath= (rc.contextPath)!''/>
<#assign cdnFileVersion = versionId!"0"/>

<#--
输出js文件 / css文件，含版本号
-->
<#macro jsFile file=[] file2=[]>
<#list file2 as x><script src="${x}"></script>
</#list>
<#list file as x><script src="${contextPath}/${x}?v=${cdnFileVersion}"></script>
</#list>
</#macro>
<#macro cssFile file=[] file2=[]>
<#list file2 as x><link rel="stylesheet" href="${x}"/>
</#list>
<#list file as x><link rel="stylesheet" href="${contextPath}/${x}?v=${cdnFileVersion}"/>
</#list>
</#macro>

<#--
文档声明/head
支持对head内容项进行修改
-->
<#macro htmHead title="" keywords="" description="" css=[] otherCss=[] js=[] otherJs=[] clean=false namespace="">
<!DOCTYPE HTML>
<html<#if namespace?length gt 0> ${namespace?no_encode}</#if>>
<head>
<link rel="shortcut icon" href="/favicon.ico"/>
<meta name="keywords" content="${keywords}"/>
<meta name="description" content="${description}"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>${title}_网易美美</title>
<@cssFile file=["css/base.css"] />
<#if !clean><@cssFile file=["css/core.css"] />
</#if>
<@cssFile file=css file2=otherCss/>
<#if !clean><script src="${contextPath}/js/jquery-1.4.2.js?v=201205221605"></script>
<@jsFile file=["js/easyCore.js"] />
</#if>
<@jsFile file=js file2=otherJs/>
<#nested>
</head>
<body>
<#-- No Script -->
<noscript><div id="noScript">
<div><h2>请开启浏览器的Javascript功能</h2><p>亲，没它我们玩不转啊！求您了，开启Javascript吧！<br/>不知道怎么开启Javascript？那就请<a href="http://www.baidu.com/s?wd=%E5%A6%82%E4%BD%95%E6%89%93%E5%BC%80Javascript%E5%8A%9F%E8%83%BD" target="_blank">猛击这里</a>！</p></div>
</div></noscript>
</#macro>

<#--
顶部导航彩票（主导航＋二级导航）
-->
<#macro topHeader tab="home" subTab="" internal=false menuObj={"childsList":[],"id":0} curMenuId=-1>
<header id="topHeader">
<a href="#"><h1 title="网易商城管理后台"><span>网易商城管理后台</span></h1></a>
<p class="loginOut"><span>${userRealName!"用户"}你好！欢迎来到网易商城管理后台，</span><a href="/logout.do">退出</a></p>
<#-- 输出一级菜单 -->
<#local menuList = menuObj.childsList![]/>
<ul id="mainMenu">
	<#list menuList as menu>
	<li pid="menu${menu.id}"><a<#if menu.id == curMenuId || _subMenuMatched(menu,curMenuId)> class="active"</#if> href="${menu.url!'/'}"><span>${menu.name}</span></a></li>
	</#list>
</ul>
<b class="topShadow"></b>
<#-- 输出二级菜单 -->
<#list menuList as menu>
	<#local subMenuList = menu.childsList![]/>
	<#if subMenuList?size gt 0>
<div id="menu${menu.id}SubMenu" class="topSubMenu<#--<#if curMenuId==menu.id || _subMenuMatched(menu,curMenuId)> fixMenu</#if>-->"><ul>
		<#list subMenuList as subMenu>
	<li><a<#if curMenuId==subMenu.id> class="active"</#if> href="${subMenu.url}"><span>${subMenu.name}</span></a></li>
		</#list>
</ul></div>
	</#if>
</#list>
<div id="globalAJAXTip">加载中&hellip;</div>
</header>
</#macro>
<#--
 查询子目录中是否激活
-->
<#function _subMenuMatched menuObj curMenuId>
	<#local menuList = menuObj.childsList![]/>
	<#local hit = false/>
	<#list menuList as menu>
		<#if menu.id == curMenuId>
			<#local hit = true/>
		</#if>
	</#list>
	<#return hit/>
</#function>

<#--
文档页脚,含章鱼统计
-->
<#macro htmFoot js=[] quickInit=true>
<#if quickInit><script>Core.quick && Core.quick();Core.quickInit && Core.quickInit();</script>
</#if>
<@jsFile file=js />
<#nested>
<#--<script src="http://analytics.163.com/ntes.js"></script>
<script>_ntes_nacc="baojian";neteaseTracker();neteaseClickStat();</script>-->
</body>
</html></#macro>

<#------------------------------------------------------------------------------------
	服务器端分页组件(splitPages)，代码逻辑同js组件 ajaxPaging.js
	maxRecordNum=0 //最大记录数量
	currentPage=1	//当前页
	totalPage=1	//最大页数
	urlTmpl="?page={p}" //url链接模版，{p}将被特定的页数替换
	nearPageNum=3	//当前页附近的页数
	wrapCss="splitPages" //包装容器的样式
-->
<#macro _linkRangeForSP a z i url><#list a..z as k><#if i==k><span>${k}</span><#else><a href="${url?replace('{p}', k)}">${k}</a></#if></#list></#macro>
<#macro splitPages maxRecordNum=0 totalPage=1 currentPage=1 nearPageNum=3 urlTmpl="?page={p}" wrapCss="splitPages">
<#local endMaxLen = nearPageNum * 2 + 2/>
<#if totalPage gt 1>
	<div class="${wrapCss}">第<b>${currentPage}</b>页 共<b>${totalPage}</b>页 总<b>${maxRecordNum}</b>条 
		<#if currentPage == 1 >上一页<#else><a href="${urlTmpl?replace('{p}', currentPage-1)}">上一页</a></#if>
		<#if (totalPage <= endMaxLen + 1 )> <#-- 页少，直接输出 -->
			<@_linkRangeForSP a=1 z=totalPage i=currentPage url=urlTmpl/>
		<#elseif (currentPage < nearPageNum + 3)> <#--情况一: 1 2 3 4 5 6 7 8 ... n -->
			<@_linkRangeForSP a=1 z=endMaxLen i=currentPage url=urlTmpl/><em>...</em><@_linkRangeForSP a=totalPage z=totalPage i=currentPage url=urlTmpl/>
		<#elseif (currentPage > totalPage - nearPageNum - 2)> <#-- 情况三: 1 ... 11 12 13 14 15 16 17 18 -->
			<@_linkRangeForSP a=1 z=1 i=currentPage url=urlTmpl/><em>...</em><@_linkRangeForSP a=totalPage-endMaxLen+1 z=totalPage i=currentPage url=urlTmpl/>
		<#else> <#--情况二: 1 ... 3 4 5 * 7 8 9 ... n -->
			<@_linkRangeForSP a=1 z=1 i=currentPage url=urlTmpl/><em>...</em><@_linkRangeForSP a=currentPage-nearPageNum z=currentPage+nearPageNum i=currentPage url=urlTmpl/><em>...</em><@_linkRangeForSP a=totalPage z=totalPage i=currentPage url=urlTmpl/>
		</#if>
		<#if currentPage == totalPage >下一页<#else><a href="${urlTmpl?replace('{p}', currentPage+1)}">下一页</a></#if>
	</div>
</#if>
</#macro>