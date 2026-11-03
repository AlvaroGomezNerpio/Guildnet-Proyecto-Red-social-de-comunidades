import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../../services/post.service';
import { PostCreateDTO } from '../../../models/post/PostCreateDTO';
import { errorMessage } from '../../../core/http-error';

@Component({
  selector: 'app-post-create',
  standalone: false,
  templateUrl: './post-create.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./post-create.component.css']
})
export class PostCreateComponent implements OnInit {
  postForm!: FormGroup;
  submitting = false;
  errorMessage: string | null = null;

  communityId!: number;
  profileId!: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private postService: PostService
  ) {}

  ngOnInit(): void {
  const idParam = this.route.snapshot.paramMap.get('id');
  const profileIdParam = this.route.snapshot.paramMap.get('profileId');

  if (!idParam || !profileIdParam) {
    this.errorMessage = 'No se pudo obtener el ID de la comunidad o del perfil.';
    return;
  }

  this.communityId = +idParam;
  this.profileId = +profileIdParam;

  this.postForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    content: ['', Validators.required],
    tags: ['']
  });
}


  onSubmit(): void {
    if (this.postForm.invalid || !this.profileId) return;

    this.submitting = true;
    const dto: PostCreateDTO = {
      title: this.postForm.value.title,
      content: this.postForm.value.content,
      tags: (this.postForm.value.tags as string)
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
      communityId: this.communityId
    };

    this.postService.createPost(dto).subscribe({
      next: () => {
        this.router.navigate(['/communities', this.communityId]);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = errorMessage(err, 'No se pudo crear el post.');
      }
    });
  }
}
