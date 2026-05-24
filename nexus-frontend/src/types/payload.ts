export type PayloadStatus = 'PENDING' | 'PROCESSING' | 'VALIDATED' | 'FAILED';

export interface Payload {
  id: string;
  tenantId: string;
  dataContractId: string;
  status: PayloadStatus;
  s3Key: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SubmitPayloadRequest {
  dataContractId: string;
  rawPayload: unknown;
}
