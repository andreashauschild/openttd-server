import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule, DatePipe, UpperCasePipe} from '@angular/common';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatTooltip} from '@angular/material/tooltip';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatDivider} from '@angular/material/divider';
import {FormsModule} from '@angular/forms';
import {Store} from '@ngrx/store';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';

import {ExplorerData} from '@api/models/explorer-data';
import {ExplorerDirectory} from '@api/models/explorer-directory';
import {ExplorerFile} from '@api/models/explorer-file';
import {selectExplorerData} from '@store/selectors/app.selectors';
import {
  createExplorerDir,
  deleteExplorerFile,
  downloadExplorerZip,
  downloadSelectedExplorerZip,
  loadExplorerData,
  renameExplorerFile
} from '@store/actions/app.actions';
import {FileUploadDialogComponent} from '@shared/ui/file-upload-dialog/file-upload-dialog.component';
import {ServerFileType} from '@api/models/server-file-type';

interface BreadcrumbItem {
  name: string;
  path: string;
}

@Component({
  selector: 'app-filexplorer',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    UpperCasePipe,
    MatIcon,
    MatButton,
    MatIconButton,
    MatCheckbox,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger,
    MatDivider,
    FormsModule
  ],
  templateUrl: './file-explorer.component.html',
  styleUrls: ['./file-explorer.component.scss']
})
export class FileExplorerComponent implements OnInit, OnDestroy {
  explorerData: ExplorerData | undefined;
  currentPath: string = '';
  breadcrumbs: BreadcrumbItem[] = [];
  currentDirectory: ExplorerDirectory | undefined;
  currentDirectoryDirs: ExplorerDirectory[] = [];
  currentDirectoryFiles: ExplorerFile[] = [];

  selectedItems: Set<string> = new Set();
  allSelected: boolean = false;

  sortColumn: 'name' | 'size' | 'type' | 'lastModified' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  private sub = new Subscription();

  constructor(
    private store: Store<{}>,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.store.dispatch(loadExplorerData({src: FileExplorerComponent.name}));
    this.sub.add(
      this.store.select(selectExplorerData)
        .pipe(filter(d => d != null))
        .subscribe(data => {
          this.explorerData = data;
          this.navigateTo(this.currentPath);
        })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  navigateTo(path: string): void {
    this.currentPath = path;
    this.selectedItems.clear();
    this.allSelected = false;
    this.buildBreadcrumbs();

    if (!this.explorerData?.directories) {
      this.currentDirectoryDirs = [];
      this.currentDirectoryFiles = [];
      return;
    }

    // Find the directory matching the current path
    const normalizedPath = path.startsWith('/') ? path : '/' + path;
    const targetPath = path === '' ? '' : normalizedPath;

    this.currentDirectory = this.explorerData.directories.find(d =>
      d.relativePath === targetPath || (targetPath === '' && d.relativePath === '')
    );

    if (this.currentDirectory) {
      // Clone the array to avoid mutating frozen NgRx store data
      this.currentDirectoryFiles = [...(this.currentDirectory.files || [])];
    } else {
      this.currentDirectoryFiles = [];
    }

    // Find subdirectories (directories whose parent path matches current path)
    // Clone the filtered array to avoid mutating frozen NgRx store data
    this.currentDirectoryDirs = [...this.explorerData.directories.filter(d => {
      if (!d.relativePath) return false;
      const parentPath = this.getParentPath(d.relativePath);
      return parentPath === targetPath;
    })];

    this.sortItems();
  }

  private getParentPath(path: string): string {
    if (!path || path === '/' || path === '') return '';
    const parts = path.split('/').filter(p => p);
    parts.pop();
    return parts.length === 0 ? '' : '/' + parts.join('/');
  }

  buildBreadcrumbs(): void {
    this.breadcrumbs = [{name: 'Home', path: ''}];

    if (this.currentPath && this.currentPath !== '') {
      const parts = this.currentPath.split('/').filter(p => p);
      let accumulatedPath = '';
      for (const part of parts) {
        accumulatedPath += '/' + part;
        this.breadcrumbs.push({name: part, path: accumulatedPath});
      }
    }
  }

  navigateUp(): void {
    const parentPath = this.getParentPath(this.currentPath);
    this.navigateTo(parentPath);
  }

  openFolder(dir: ExplorerDirectory): void {
    if (dir.relativePath) {
      this.navigateTo(dir.relativePath);
    }
  }

  toggleSelection(item: ExplorerFile | ExplorerDirectory): void {
    const id = item.id || item.relativePath || '';
    if (this.selectedItems.has(id)) {
      this.selectedItems.delete(id);
    } else {
      this.selectedItems.add(id);
    }
    this.updateAllSelectedState();
  }

  toggleSelectAll(): void {
    if (this.allSelected) {
      this.selectedItems.clear();
    } else {
      this.currentDirectoryDirs.forEach(d => {
        if (d.id) this.selectedItems.add(d.id);
      });
      this.currentDirectoryFiles.forEach(f => {
        if (f.id) this.selectedItems.add(f.id);
      });
    }
    this.allSelected = !this.allSelected;
  }

  private updateAllSelectedState(): void {
    const totalItems = this.currentDirectoryDirs.length + this.currentDirectoryFiles.length;
    this.allSelected = totalItems > 0 && this.selectedItems.size === totalItems;
  }

  isSelected(item: ExplorerFile | ExplorerDirectory): boolean {
    const id = item.id || '';
    return this.selectedItems.has(id);
  }

  createFolder(): void {
    const folderName = prompt('Enter folder name:');
    if (folderName && folderName.trim()) {
      const newPath = this.currentPath
        ? `${this.currentPath}/${folderName.trim()}`
        : folderName.trim();
      this.store.dispatch(createExplorerDir({
        src: FileExplorerComponent.name,
        relativeDirPath: newPath
      }));
    }
  }

  uploadFiles(): void {

    const dialogRef = this.dialog.open(FileUploadDialogComponent, {minWidth: "800px"});
    dialogRef.componentInstance.dialogRef = dialogRef;
    dialogRef.componentInstance.fileType = ServerFileType.OpenttdRoot;
    dialogRef.componentInstance.targetDir = this.currentPath!;
    dialogRef.componentInstance.dialogTitle = "UPLOAD OPENTTD CONFIGS";
    dialogRef.componentInstance.subTitle = "Info: Don't upload files where the filename contains single/double quotes: ' or \" . This will cause problems!";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
      this.refresh();
    });
  }

  refresh(): void {
    this.store.dispatch(loadExplorerData({src: FileExplorerComponent.name}));
  }

  deleteSelected(): void {
    if (this.selectedItems.size === 0) return;

    const confirmMsg = `Are you sure you want to delete ${this.selectedItems.size} item(s)?`;
    if (!confirm(confirmMsg)) return;

    // Get all selected items and delete them
    const allItems = [...this.currentDirectoryDirs, ...this.currentDirectoryFiles];
    allItems.forEach(item => {
      if (this.selectedItems.has(item.id || '')) {
        const relativePath = 'relativePath' in item ? item.relativePath : '';
        if (relativePath) {
          this.store.dispatch(deleteExplorerFile({
            src: FileExplorerComponent.name,
            relativePath: relativePath
          }));
        }
      }
    });

    this.selectedItems.clear();
    this.allSelected = false;
  }

  downloadSelected(): void {
    if (this.selectedItems.size === 0) return;

    const fileNames: string[] = [];
    const allItems = [...this.currentDirectoryDirs, ...this.currentDirectoryFiles];

    allItems.forEach(item => {
      if (this.selectedItems.has(item.id || '')) {
        if (item.name) {
          fileNames.push(item.name);
        }
      }
    });

    if (fileNames.length > 0) {
      this.store.dispatch(downloadSelectedExplorerZip({
        src: FileExplorerComponent.name,
        directoryPath: this.currentPath,
        fileNames: fileNames
      }));
    }
  }

  downloadFile(file: ExplorerFile): void {
    if (file.relativePath) {
      const url = `/api/openttd-server/explorer/download?fileName=${encodeURIComponent(file.relativePath)}`;
      window.open(url, '_blank');
    }
  }

  downloadDirectory(dir: ExplorerDirectory): void {
    if (dir.relativePath) {
      this.store.dispatch(downloadExplorerZip({
        src: FileExplorerComponent.name,
        directoryPath: dir.relativePath
      }));
    }
  }

  deleteItem(item: ExplorerFile | ExplorerDirectory): void {
    const name = item.name || 'this item';
    if (!confirm(`Are you sure you want to delete "${name}"?`)) return;

    const relativePath = 'relativePath' in item ? item.relativePath : '';
    if (relativePath) {
      this.store.dispatch(deleteExplorerFile({
        src: FileExplorerComponent.name,
        relativePath: relativePath
      }));
    }
  }

  renameItem(item: ExplorerFile | ExplorerDirectory): void {
    const currentName = item.name || '';
    const newName = prompt('Enter new name:', currentName);

    if (newName && newName.trim() && newName !== currentName) {
      const relativePath = 'relativePath' in item ? item.relativePath : '';
      if (relativePath) {
        this.store.dispatch(renameExplorerFile({
          src: FileExplorerComponent.name,
          relativePath: relativePath,
          newName: newName.trim()
        }));
      }
    }
  }

  sort(column: 'name' | 'size' | 'type' | 'lastModified'): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.sortItems();
  }

  private sortItems(): void {
    const direction = this.sortDirection === 'asc' ? 1 : -1;

    // Sort directories
    this.currentDirectoryDirs.sort((a, b) => {
      const aName = a.name || '';
      const bName = b.name || '';
      return aName.localeCompare(bName) * direction;
    });

    // Sort files
    this.currentDirectoryFiles.sort((a, b) => {
      switch (this.sortColumn) {
        case 'name':
          return (a.name || '').localeCompare(b.name || '') * direction;
        case 'size':
          return ((a.size || 0) - (b.size || 0)) * direction;
        case 'type':
          return (a.extension || '').localeCompare(b.extension || '') * direction;
        case 'lastModified':
          return ((a.lastModified || 0) - (b.lastModified || 0)) * direction;
        default:
          return 0;
      }
    });
  }

  formatSize(bytes: number | undefined): string {
    if (bytes === undefined || bytes === null) return '—';
    if (bytes === 0) return '0 B';

    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    const size = bytes / Math.pow(1024, i);

    return `${size.toFixed(i > 0 ? 2 : 0)} ${units[i]}`;
  }

  getFileIcon(file: ExplorerFile): string {
    const ext = (file.extension || '').toLowerCase();

    const iconMap: { [key: string]: string } = {
      'pdf': 'picture_as_pdf',
      'doc': 'description',
      'docx': 'description',
      'xls': 'table_chart',
      'xlsx': 'table_chart',
      'txt': 'article',
      'md': 'article',
      'json': 'data_object',
      'xml': 'code',
      'html': 'code',
      'css': 'code',
      'js': 'code',
      'ts': 'code',
      'py': 'code',
      'java': 'code',
      'png': 'image',
      'jpg': 'image',
      'jpeg': 'image',
      'gif': 'image',
      'svg': 'image',
      'mp3': 'audio_file',
      'wav': 'audio_file',
      'mp4': 'video_file',
      'avi': 'video_file',
      'zip': 'folder_zip',
      'rar': 'folder_zip',
      '7z': 'folder_zip',
      'tar': 'folder_zip',
      'gz': 'folder_zip',
      'sav': 'save',
      'cfg': 'settings',
      'ini': 'settings',
      'log': 'receipt_long',
      'grf': 'extension',
      'nut': 'code',
      'lng': 'translate'
    };

    return iconMap[ext] || 'insert_drive_file';
  }

  getTotalSize(): number {
    if (!this.currentDirectoryFiles) return 0;
    return this.currentDirectoryFiles.reduce((sum, f) => sum + (f.size || 0), 0);
  }

  getItemCount(): number {
    return this.currentDirectoryDirs.length + this.currentDirectoryFiles.length;
  }
}
