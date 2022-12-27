const l = location

export const environment = {
  production: true,
  baseUrl: l.protocol + '//' + l.host,
  wsServerRoot: 'ws://' + l.host
};
