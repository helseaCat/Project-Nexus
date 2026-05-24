export type Severity = 'WARNING' | 'CRITICAL';

export interface AlignmentExpectation {
  id: string;
  tenantId: string;
  dataContractId: string;
  name: string;
  description: string;
  severity: Severity;
  ruleExpression: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CreateExpectationRequest {
  dataContractId: string;
  name: string;
  description: string;
  severity: Severity;
  ruleExpression: string;
}

export interface UpdateExpectationRequest {
  name?: string;
  description?: string;
  severity?: Severity;
  ruleExpression?: string;
}
