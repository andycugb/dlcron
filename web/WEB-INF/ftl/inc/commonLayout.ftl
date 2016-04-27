<#--
	通用布局ftl
	需要supplierList数据
-->
<#macro leftNav supplierList>
<#if supplierList?size gt 1 >
<div id="listNav">
	<div id="navSearchBox" class="hideWhenMini">
		<span class="mcInputBox"><span><input value="" id="navSearchInput" autoComplete="false" placeholder="搜索供应商名称"/></span></span><a class="defBtn"><span>搜索</span></a>
	</div>
	<div id="togListNav"><em></em><span><b><i>◆</i></b></span></div>
	<div id="supplierList" class="hideWhenMini">
		<b><a href="javascript:;" class="active"><span>全部供应商</span></a></b>
		<#list supplierList as sp>
		<a href="javascript:;" sid="${sp.supplierId}" title="${sp.name}"><span>${sp.name}</span></a>
		</#list>
	</div>
</div>
</#if>
</#macro>