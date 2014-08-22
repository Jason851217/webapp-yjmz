package com.minyisoft.webapp.yjmz.common.service.impl;

import java.io.InputStream;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.minyisoft.webapp.core.model.criteria.PageDevice;
import com.minyisoft.webapp.core.service.impl.BaseServiceImpl;
import com.minyisoft.webapp.yjmz.common.model.WorkFlowConfigInfo;
import com.minyisoft.webapp.yjmz.common.model.criteria.WorkFlowConfigCriteria;
import com.minyisoft.webapp.yjmz.common.model.enumField.WorkFlowStatusEnum;
import com.minyisoft.webapp.yjmz.common.persistence.WorkFlowConfigDao;
import com.minyisoft.webapp.yjmz.common.security.SecurityUtils;
import com.minyisoft.webapp.yjmz.common.service.WorkFlowConfigService;

@Service("workFlowConfigService")
public class WorkFlowConfigServiceImpl extends
		BaseServiceImpl<WorkFlowConfigInfo, WorkFlowConfigCriteria, WorkFlowConfigDao> implements WorkFlowConfigService {
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	//@Autowired
	//private IdentityService identityService;
	@Autowired
	private HistoryService historyService;

	@Override
	protected void _validateDataBeforeDelete(WorkFlowConfigInfo info) {
		SecurityUtils.checkIsCurrentUserAdministrator();
		Assert.isTrue(info.getWorkFlowStatus() != WorkFlowStatusEnum.NORMAL,
				"不允许删除状态为[" + WorkFlowStatusEnum.NORMAL.getDescription() + "]的工作流");
	}

	@Override
	protected void _validateDataBeforeAdd(WorkFlowConfigInfo info) {
		SecurityUtils.checkIsCurrentUserAdministrator();
	}

	@Override
	protected void _validateDataBeforeSave(WorkFlowConfigInfo info) {
		SecurityUtils.checkIsCurrentUserAdministrator();
	}

	@Override
	public void delete(WorkFlowConfigInfo info) {
		super.delete(info);
		// 删除流程定义
		repositoryService.deleteDeployment(repositoryService.getProcessDefinition(info.getProcessDefinitionId())
				.getDeploymentId(), true);
	}

	@Override
	public void deployWorkFlow(WorkFlowConfigInfo config, String processDefinitionFileName,
			InputStream processDefinitionFile) {
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
				.category(config.getDefineOrg().getId()).enableDuplicateFiltering().name("Tex100SystemDeployment");
		deploymentBuilder.addInputStream(processDefinitionFileName, processDefinitionFile);
		Deployment deploy = deploymentBuilder.deploy();
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId())
				.singleResult();

		config.setProcessDefinitionId(pd.getId());
		config.setName(pd.getName());
		submit(config);
	}

	@Override
	public void activateProcessDefinition(WorkFlowConfigInfo config) {
		Assert.notNull(config, "待激活工作流不能为空");
		Assert.isTrue(config.getWorkFlowStatus() == WorkFlowStatusEnum.SUSPEND, "待激活工作流状态异常");

		config.setWorkFlowStatus(WorkFlowStatusEnum.NORMAL);
		save(config);
		repositoryService.activateProcessDefinitionById(config.getProcessDefinitionId(), true, null);
	}

	@Override
	public void suspendProcessDefinition(WorkFlowConfigInfo config) {
		Assert.notNull(config, "待挂起工作流不能为空");
		Assert.isTrue(config.getWorkFlowStatus() == WorkFlowStatusEnum.NORMAL, "待挂起工作流状态异常");

		config.setWorkFlowStatus(WorkFlowStatusEnum.SUSPEND);
		save(config);
		repositoryService.suspendProcessDefinitionById(config.getProcessDefinitionId(), true, null);
	}

	@Override
	public List<HistoricProcessInstance> getProcessInstances(String processDefinitionId, PageDevice pageDevice) {
		Assert.hasText(processDefinitionId, "流程定义ID不能为空");
		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
				.processDefinitionId(processDefinitionId).orderByProcessInstanceId().desc();
		if (pageDevice != null) {
			pageDevice.setTotalRecords((int) query.count());
			return query.listPage(pageDevice.getStartRowNumberOfCurrentPage() - 1, pageDevice.getRecordsPerPage());
		} else {
			return query.list();
		}
	}

	/*@Override
	public Optional<String> startProcess(SupportedWorkFlowTypeEnum workFlowType, ISystemOrgObject owner,
			IWorkFlowBusinessModel businessKey, Map<String, Object> processVariables) {
		Assert.notNull(workFlowType);
		Assert.notNull(owner);
		WorkFlowConfigCriteria criteria = new WorkFlowConfigCriteria();
		criteria.setWorkFlowStatus(WorkFlowStatusEnum.NORMAL);
		criteria.setDefineOrg(owner);
		criteria.setWorkFlowType(workFlowType);
		WorkFlowConfigInfo config = find(criteria);
		if (config != null && StringUtils.isNotBlank(config.getProcessDefinitionId())) {
			// 启动工作流
			try {
				identityService.setAuthenticatedUserId(SecurityUtils.getCurrentUser().getId());
				ProcessInstance instance = runtimeService.startProcessInstanceById(config.getProcessDefinitionId(),
						businessKey != null ? businessKey.getId() : null, processVariables);
				return Optional.of(instance.getId());
			} finally {
				identityService.setAuthenticatedUserId(null);
			}
		}
		return Optional.absent();
	}*/

	@Override
	public void deleteRunningProcess(String processInstanceId) {
		runtimeService.deleteProcessInstance(processInstanceId, null);
		historyService.deleteHistoricProcessInstance(processInstanceId);
	}
}