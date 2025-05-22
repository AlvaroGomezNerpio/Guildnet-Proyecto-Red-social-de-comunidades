import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PostService } from '../../../services/post.service';
import { PostCreateDTO } from '../../../models/post/PostCreateDTO';

@Component({
  selector: 'app-post-create',
  standalone: false,
  templateUrl: './post-create.component.html',
  styleUrls: ['./post-create.component.css'],
})
export class PostCreateComponent implements OnInit {
  @Input() communityId!: number;
  @Input() profileId!: number;
  @Output() postCreated = new EventEmitter<void>();

  postForm!: FormGroup;
  submitting = false;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private postService: PostService) {}

  ngOnInit(): void {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
      tags: [''],
    });
  }

  onSubmit(): void {
    if (this.postForm.invalid) return;

    this.submitting = true;
    this.errorMessage = null;

    const dto: PostCreateDTO = {
      title: this.postForm.value.title,
      content: this.postForm.value.content,
      tags: this.parseTags(this.postForm.value.tags),
      profileId: this.profileId,
      communityId: this.communityId,
    };

    this.postService.createPost(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.postForm.reset();
        this.postCreated.emit(); // Emite evento para refrescar la lista de posts
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'No se pudo crear el post';
      },
    });
  }

  private parseTags(rawTags: string): string[] {
    return rawTags
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
  }
}
