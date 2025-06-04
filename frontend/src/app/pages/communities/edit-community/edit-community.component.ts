import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommunityService } from '../../../services/community.service';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';

@Component({
  selector: 'app-edit-community',
  standalone: false,
  templateUrl: './edit-community.component.html',
  styleUrl: './edit-community.component.css'
})
export class EditCommunityComponent implements OnInit {
  community: any;
  editForm!: FormGroup;
  imageFile?: File;
  bannerFile?: File;

  constructor(
    private route: ActivatedRoute,
    private communityService: CommunityService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.communityService.getCommunityById(id).subscribe(c => {
      this.community = c;
      this.editForm = this.fb.group({
        name: [c.name, Validators.required],
        description: [c.description],
        rules: [c.rules],
        tags: [c.tags.join(', ')]
      });
    });
  }

  get descriptionControl(): FormControl {
    return this.editForm.get('description') as FormControl;
  }

  get rulesControl(): FormControl {
    return this.editForm.get('rules') as FormControl;
  }

  onImageSelected(event: any): void {
    this.imageFile = event.target.files[0];
  }

  onBannerSelected(event: any): void {
    this.bannerFile = event.target.files[0];
  }

  onSubmit(): void {
    const id = this.community.id;
    const data = {
      name: this.editForm.value.name,
      description: this.editForm.value.description,
      rules: this.editForm.value.rules,
      tags: this.editForm.value.tags.split(',').map((t: string) => t.trim())
    };

    this.communityService.updateCommunity(id, data, this.imageFile, this.bannerFile).subscribe({
      next: (res) => {
        console.log('Actualización exitosa:', res);
        this.router.navigate(['/communities', id]);
      },
      error: err => {
        console.error('Error al actualizar la comunidad:', err);
        alert('Ocurrió un error al actualizar la comunidad');
      }
    });
  }

  onDelete(): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta comunidad?')) {
      this.communityService.deleteCommunity(this.community.id).subscribe(() => {
        this.router.navigate(['/communities']);
      });
    }
  }
}
