<#include "../inc/core.ftl">
<#include "../inc/commonLayout.ftl">
<@htmHead title="网易商城后台管理系统"/>
<@topHeader menuObj=menu curMenuId=menuId!0/> 
<#assign divid='cron'/>
<#assign ctx="${(rc.contextPath)!''}" />
<form action='${ctx}/cron/saveUpdate.do' method="post" onsubmit="return checkForm();">
	<table class="table_line">
		<tr>
			<th>cron名称</th>
			<td> 
				<#if cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH"> 
					<input type="text" name="cronName" id="cronName" size="80" value='${cron.cronName?if_exists}'><span style='color:red;'>*</span>
				<#else>
					${cron.cronName?if_exists}
					<input type="hidden" name="cronName" id="cronName" size="80" value='${cron.cronName?if_exists}'>
				</#if>
			</td>
		</tr>
		<tr>
			<th>执行方法</th>
			<td>
				<#if cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH">
					<input type="text" name="serviceName" id="serviceName" size="80" value='${cron.serviceName?if_exists}'><span style='color:red;'>*</span>
				<#else>
					${cron.serviceName?if_exists}
					<input type="hidden" name="serviceName" id="serviceName" size="80" value='${cron.serviceName?if_exists}'>
				</#if>
			</td>
		</tr>
		<tr>
			<th>执行策略</th>
			<td><input type="text" name="cronExpression" id="cronExpression" size="80" value='${cron.cronExpression?if_exists}'><span style='color:red;'>*</span></td>
		</tr>
		<tr>
			<th>ip限制</th>
			<td><input type="text" name="limitIp" id="limitIp" size="80" value="${cron.limitIp?if_exists}"><span style='color:red;'>*</span>
				<br>注：多个IP用分号";"分隔；填入0.0.0.0表示在所有机器上"执行"；填入1.1.1.1代表在所有机器上都"不执行"。0.0.0.0或1.1.1.1不可与其他IP共存。
		</tr>
		<tr>
			<th>启动时是否执行</th>
			<td>
				<#if cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH">
					<select name="fireOnStartUp" id="fireOnStartUp" >
						<option value="0"<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==0> selected</#if>>
							不执行
						</option>
						<option value="1"<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==1> selected</#if>>
							执行
						</option>
					</select>
				<#else>
					<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==0>不执行
						<input type="hidden" name="fireOnStartUp" id="fireOnStartUp" size="80" value='${cron.fireOnStartUp}'>
					</#if>
					<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==1>执行
						<input type="hidden" name="fireOnStartUp" id="fireOnStartUp" size="80" value='${cron.fireOnStartUp}'>
					</#if>
				</#if>
			</td>
		</tr>
		<tr><th>所属系统</th>
			<td>
				<#if cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH">
					<select name="groupName" id="groupName" >
						<option value="">
							请选择
						</option>
						<#if cronGroupNames?exists&&cronGroupNames!="">
							<#list cronGroupNames?split(";") as cronGroupName>
								
								<option value="${cronGroupName?if_exists}"<#if cron.groupName?exists&&cron.groupName==cronGroupName> selected</#if>>
									${cronGroupName?if_exists}
								</option>
							</#list>
						</#if>
					</select>
				<#else>
					${groupName?if_exists}
					<input type="hidden" name="groupName" id="groupName" size="80" value='${cron.groupName?if_exists}'>
				</#if>
			</td></tr>
		<tr>
			<th>描述</th>
			<td>
				<#if cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH">
					<input type="text" name="cronDesc" size="80" value="${cron.cronDesc?if_exists}">
				<#else>
					${cron.cronDesc?if_exists}
					<input type="hidden" name="cronDesc" id="cronDesc" size="80" value='${cron.cronDesc?if_exists}'>
				</#if>
			</td>
		</tr>
		<#if opResult??>
			<tr>
				<td colspan=2><span style='color:red;'>${opResult}</span></td>
			</tr>
		</#if>
		<tr>
			<input type="hidden" name="id" value="${cron.id}"/>
			<td colspan=2><input type="submit" value="提交"/></td>
		</tr>
	</table>
</from>
<script language="JavaScript" type="text/javascript">
			 		function checkForm(){ 
			   //Cron名称校验规则：不能为空；不能包含"/"字符或空格
			    var cronName=document.getElementById("cronName").value; 		   
			   if(cronName==""){
			         alert('Cron名称不能为空!');
			         document.getElementById("cronName").focus;
		       return false;
			         } else if(cronName.indexOf(" ")!=-1){  
		      alert('Cron名称不能包含空格!');
			         document.getElementById("cronName").focus;
		       return false;
		      	} else if(cronName.indexOf("/")!=-1){  
		      alert('Cron名称不能包含"/"!');
			         document.getElementById("cronName").focus;
		       return false;
		      	}          
		      	//Cron执行方法不能为空 
			    if(document.getElementById("serviceName").value==""){
			         alert('Cron执行方法不能为空!');
			         document.getElementById("serviceName").focus;
	           return false;
			         }
			         //else if(!service_name_verify(serviceName)){{
			         //alert('Cron执行方法格式有误，未通过正则校验。请更正!');
			         //document.getElementById("serviceName").focus;
	           //return false;
			     //    } 
			         //Cron执行策略不能为空
			    if(document.getElementById("cronExpression").value==""){
			         alert('Cron执行策略不能为空!');
			         document.getElementById("cronExpression").focus;
	           return false;
			         }  
			         
			         //IP限制字段校验规则：不能为空；多个IP之间以“分号”作为分隔符；0.0.0.0或1.1.1.1不可与其他IP共存;每个IP必须符合IP地址规范。
			         var limitIp=document.getElementById("limitIp").value;
			    if(limitIp==""){
			         alert('Cron执行IP限制不能为空!');
			         document.getElementById("limitIp").focus;
	                 return false;
			         }else{
			         var limitIpArray= limitIp.split(";");
			         
			         if((limitIp.indexOf("0.0.0.0")!=-1||limitIp.indexOf("1.1.1.1")!=-1)&&limitIpArray.length > 1){
			           alert('"0.0.0.0"或"1.1.1.1"不能与其他IP共存!');
			         document.getElementById("limitIp").focus;
	                 return false;		         
			         } 
			         for (i=0;i<limitIpArray.length ;i++ )    
	                { 
			         if(!ip_address_verify(limitIpArray[i])){      
			         alert('IP限制字段提供的IP地址不符合规范，或未以约定的分号";"作为分隔符!');
			         document.getElementById("limitIp").focus;
	                  return false;
	                 } 
	                } 
			    }   
			       
		 	    return true;
			  } 
	//IP地址格式校验		  
	  function ip_address_verify(addr) 
	   { 
	     var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])(\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])){3}$/;
	     if(addr.match(reg))
	     {
	        return true;
	     }
	     else 
	     {
	       return false;
	     }
		} 
		
		
	//Service Name格式校验		  
	  function service_name_verify(serviceName) 
	   { 
	     var reg = /^((()|(void )|(boolean )|(Boolean )|(String )|(int )|(Integer )|(float )|(long )|(double )|(Float )|(Long )|(Double ))[a-z]{1}[^, ]*\\((((boolean )|(int )|(float )|(long )|(double )|(String )|(Boolean )|(Integer )|(Float )|(Long )|(Double ))[a-z]{1}[^, ]*,?)*\\);?)+$/;
	     if(serviceName.match(reg))
	     {
	        return true;
	     }
	     else 
	     {
	       return false;
	     }
		} 
		
		  	        
		
</script>
<@htmFoot />