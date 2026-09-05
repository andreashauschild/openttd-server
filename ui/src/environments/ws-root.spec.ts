import {toWebSocketRoot} from './ws-root';

describe('toWebSocketRoot', () => {

  it('should use wss:// on a https page', () => {
    expect(toWebSocketRoot({protocol: 'https:', host: 'openttd.example.com'})).toBe('wss://openttd.example.com');
  });

  it('should use ws:// on a http page', () => {
    expect(toWebSocketRoot({protocol: 'http:', host: 'openttd.example.com'})).toBe('ws://openttd.example.com');
  });

  it('should keep the port of the host', () => {
    expect(toWebSocketRoot({protocol: 'https:', host: 'localhost:14003'})).toBe('wss://localhost:14003');
    expect(toWebSocketRoot({protocol: 'http:', host: 'localhost:8080'})).toBe('ws://localhost:8080');
  });
});
