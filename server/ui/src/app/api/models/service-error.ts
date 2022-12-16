/* tslint:disable */
/* eslint-disable */
import { ServiceErrorType } from './service-error-type';
export interface ServiceError {
  message?: string;
  stackTrace?: string;
  type?: ServiceErrorType;
}
