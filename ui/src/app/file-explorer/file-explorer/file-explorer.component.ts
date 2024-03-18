import {Component} from '@angular/core';
import {DataService, ExplorerComponent, INode, NAME_FUNCTION} from 'ngx-explorer';
import {FileExplorerDataService} from './file-explorer-data.service';

@Component({
  selector: 'app-file-explorer',
  standalone: true,
  imports: [ExplorerComponent],

  templateUrl: './file-explorer.component.html',
  styleUrl: './file-explorer.component.scss'
})
export class FileExplorerComponent {

}
