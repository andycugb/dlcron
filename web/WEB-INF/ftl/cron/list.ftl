
<#include "../inc/core.ftl">
<#include "../inc/commonLayout.ftl">
<@htmHead title="网易商城后台管理系统"/>
<@topHeader menuObj=menu curMenuId=menuId!0/>
<#assign divid='cron'/>
<#assign ctx="${(rc.contextPath)!''}" />
<div class="docBody" id="listCommonWrap">
	<div id="content_Box" class="clearfix"><div id="contentWrap">
			<div id="dataTableWrap">
				<div id="virtualHeadTableBox">
					<form name='${divid}listform' action='${ctx}/cron/list.do' method="post">
						<input type='hidden' name='id' id='id'>
						<input type='hidden' name='cronRefreshUrlInfo' id='cronRefreshUrlInfo'>
						
						<table class="table_line">
							<tr><th class="tc" colspan="4" ><b>Cron查询</b></th></tr>
							<tr>
								<th width="100px" style="text-align:left;">CronName:</th>
								<td style="text-align:left;">
									<input type="text" id="cronName" name="cronName" value="${cronQueryCond.cronName?if_exists}" maxlength="100" size="25"/>
								</td>
								<th width="100px" style="text-align:left;">Cron描述：</th>
								<td style="text-align:left;">
									<input type="text" id="cronDesc" name="cronDesc" value="${cronQueryCond.cronDesc?if_exists}" maxlength="100" size="25"/>
								</td>
							</tr>
							<tr>
								<th width="90px" style="text-align:left;">启动时是否执行</th>
								<td style="text-align:left;"><select name="fireOnStartUp" id="fireOnStartUp" >
										<option value="">
											请选择
										</option>
										<option value="0"<#if cronQueryCond.fireOnStartUp?exists&&cronQueryCond.fireOnStartUp==0> selected</#if>>
											不执行
										</option>
										<option value="1"<#if cronQueryCond.fireOnStartUp?exists&&cronQueryCond.fireOnStartUp==1> selected</#if>>
											执行
										</option>
									</select></td>
								<th width="90px" style="text-align:left;">所属系统</th>
								<td style="text-align:left;">
									<select name="groupName" id="groupName" >
										<option value="">
											请选择
										</option>
										<#if cronGroupNames?exists&&cronGroupNames!="">
											<#list cronGroupNames?split(";") as cronGroupName>
												<option value="${cronGroupName?if_exists}"<#if cronQueryCond.groupName?exists&&cronQueryCond.groupName==cronGroupName> selected</#if>>
													${cronGroupName?if_exists}
												</option>
											</#list>
										</#if> 
									</select></td>
							</tr>
							<tr>
								<td colspan="4" class="tc">
									<input type="submit" id="searchBut" name="searchBut" value="查&nbsp;&nbsp;&nbsp;询" />
								</td>
							</tr>
						</table>
					</form>
				</div>
				<div id="dataTableBox">
					<div id="dataTableScrollBox">
						<br>
						[
						<a href='${ctx}/cron/add.do' target='_blank'>新增</a>]&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp; [
									 <a href="#" onclick="refreshCron();return false;">刷新Cron配置</a>
									 <select name="cronRefreshTargetGroup" id="cronRefreshTargetGroup" > 
										<#if cronGroupNames?exists&&cronGroupNames!="">
											<#list cronGroupNames?split(";") as cronGroupName>
												<option value="${cronGroupName?if_exists}"<#if cronQueryCond.groupName?exists&&cronQueryCond.groupName==cronGroupName> selected</#if>>
													${cronGroupName?if_exists}
												</option>
											</#list>
										</#if> 
											<#if cronGroupNamesConfirm?exists&&cronGroupNamesConfirm!="">
											<#list cronGroupNamesConfirm?split(";") as cronGroupNameConfirm>
												<option value="${cronGroupNameConfirm?if_exists}">
													${cronGroupNameConfirm?if_exists}
												</option>
											</#list>
										</#if>
										 
									</select>
									]
						<br>
						<#if opResult??>
							${opResult?no_encode}
						</#if>
						<table class="table_line" id="queryResultListTable">
							<tr>
								<th width="100">Cron名称</th>
								<th width="100">执行方法</th>
								<th width="60">执行策略</th>
								<th width="80">ip限制</th>
								<th width="30">启动时是否执行</th>
								<th width="100">描述</th>
								<th width="90">所属系统</th>
								<th width="180">立即执行Cron</th>
								<th width="80">操作</th>
							</tr>
							<#assign hascontent = (cronList?exists && ((cronList?size) gt 0))/>
							<#if hascontent>
								<#list cronList as cron>
									<tr>
										<td style="white-space:normal;word-break:break-all">${cron.cronName!''}</td>
										<td style="white-space:normal;word-break:break-all">${cron.serviceName!''}</td>
										<td style="white-space:normal;word-break:break-all">${cron.cronExpression!''}</td>
										<td style="white-space:normal;word-break:break-all">${cron.limitIp!''}</td>
										
										
										<td>
											<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==0>不执行
											</#if>
											<#if cron.fireOnStartUp?exists&&cron.fireOnStartUp==1>执行
											</#if>
										</td>
										<td style="white-space:normal;word-break:break-all">${cron.cronDesc!}</td>
										<td style="white-space:normal;word-break:break-all" align="middle">${cron.groupName!}</td>
										<td>
											<#if (cron.cronName?exists&&cron.cronName!="SYSTEM_CRON_AUTO_REFRESH")>
												 	<#if  cron.execMachines?exists&&cron.execMachines!=""> 
													<select name="${cron.id}CronExecuteIp" id="${cron.id}CronExecuteIp" >
														<#list cron.execMachines?split(";") as cronIp>
															<#if cronIp_index==0>
																<option value="0.0.0.0" selected>
																	${cronIp?if_exists}
																</option>
															<#else>
																<option value="${cronIp?if_exists}" <#if cronIp?exists&&cron.limitIp?exists&&(cron.limitIp!'*')==(cronIp!'')?split(":")[0]> selected</#if>> 
																	${cronIp?if_exists}
																</option>
															</#if>
														</#list>
														 
													</select>
													<a href="#" target="_blank" onclick="doRefresh('${cron.id}','${cron.cronName}','${cron.groupName!}'+'_machines','');return false;">立即执行</a>
												</#if> 
												<#if cron.execMachinesConfirm?exists&&cron.execMachinesConfirm!="">
													<br>
													<select name="${cron.id}CronExecuteIpOnlineConfirm" id="${cron.id}CronExecuteIpOnlineConfirm" >
														<#list cron.execMachinesConfirm?split(";") as cronIp> 
															<#if cronIp_index==0>
																<option value="0.0.0.0">
																	${cronIp?if_exists}
																</option>
															<#else>
																<option value="${cronIp?if_exists}" <#if cronIp?exists && cron.limitIp?exists &&cron.limitIp==(cronIp!'')?split(":")[0]> selected</#if>>
																	${cronIp?if_exists}
																</option>
															</#if>
														</#list>
														 
													</select>
													<a href="#" target="_blank" onclick="doRefresh('${cron.id}','${cron.cronName}','${cron.groupName!}'+'_confirm_machines','OnlineConfirm');return false;">立即执行</a>
												</#if>
												  
											</#if>
										</td>
										<td style="white-space:normal;word-break:break-all">
											<a href="${ctx}/cron/update.do?id=${cron.id}" target="_blank">修改</a>|
											<a href="#" onclick="doDelete('${cron.id}');return false;">删除</a>
										</td>
									</tr>
								</#list>
								<tr>
									<th width="100"></th>
									<th width="160"></th>
									<th width="110"></th>
									<th width="60"></th>
									<th width="60"></th>
									<th width="150"></th>
									<th width="150"></th>
									<th width="180"></th>
									<th width="100"></th>
								</tr>
								<tr><td colspan='9' align='middle'>&nbsp;&nbsp;</td></tr>
							<#else>
								<tr><td colspan='9' align='middle'><span style='color:red;'>没有符合条件的Cron！</span></td></tr>
							</#if>
						</table>
					</div>
					 
				</div>
			</div>
		</div>
	</div>
</div>
</div>
</div>

<script language="JavaScript" type="text/javascript">
		window.onload = function() {
		    doubleBgColor(document.getElementById("queryResultListTable"),"#f8fbfc","#e5f1f4");
		}
		
		function doubleBgColor(Table,Bg1,Bg2) {
		    for (var i=0;i<Table.rows.length;i++) Table.rows[i].bgColor=i%2?Bg1:Bg2;
		}
		 
		 
		function doRefresh(id,name,groupInfo,env)
		{   
		  var cronExecuteIp= document.getElementById(id+"CronExecuteIp"+env).value;
		  var cronRefreshUrl=cronExecuteIp+"/bg/cron/call/"+name+".html"; 
		  document.getElementById("cronRefreshUrlInfo").value=cronRefreshUrl; 
		  document.${divid}listform.action = '${ctx}/cron/executeCron.do?groupInfo='+groupInfo;	  
		  document.${divid}listform.submit();	  
	   }    				
	     
	   function doDelete(key)
				{
					 if(confirm("即将从数据库删除该条记录。删除后数据将不能恢复。您确实要删除吗？")){ 
					   document.getElementById("id").value=key; 
					   	  document.${divid}listform.action = '${ctx}/cron/delete.do';
						document.${divid}listform.submit();  
					 } 
				}
				 
	   function refreshCron()
				{ 
					  var group= document.getElementById("cronRefreshTargetGroup").value; 
					   	  document.${divid}listform.action = '${ctx}/cron/refresh.do?cronGroupName='+group;
						document.${divid}listform.submit();  
					  
				}  				
</script>
<@htmFoot/>
