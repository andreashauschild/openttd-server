/* tslint:disable */
/* eslint-disable */
import { DefaultRepository } from './default-repository';
export interface PauseCommand {
  cmdSend?: boolean;
  command?: string;
  executed?: boolean;
  marker?: string;
  rawResult?: string;
  repository?: DefaultRepository;
  type?: string;
}
