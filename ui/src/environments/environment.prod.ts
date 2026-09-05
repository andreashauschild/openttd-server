import {toWebSocketRoot} from './ws-root';

const l = location

export const environment = {
  production: true,
  baseUrl: l.protocol + '//' + l.host,
  wsServerRoot: toWebSocketRoot(l)
};
