import { TestBed } from '@angular/core/testing';

import { FileExplorerDataService } from './file-explorer-data.service';

describe('FileExplorerDataService', () => {
  let service: FileExplorerDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileExplorerDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
