import { Component, OnInit, ChangeDetectionStrategy, inject } from '@angular/core';
import { injectCommunityTheme } from '../../../../core/theme/community-theme.service';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PostService } from '../../services/post.service';
import { PostCategoryDTO, PostCreateDTO } from '../../models/post.models';
import { errorMessage } from '../../../../core/http/http-error';
import { QuillEditorComponent } from 'ngx-quill';
import { CategorySelectComponent } from '../../components/category-select/category-select.component';
import { PostCategoryService } from '../../services/post-category.service';


@Component({
    selector: 'app-post-create',
    templateUrl: './post-create.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrls: ['./post-create.component.css'],
    imports: [FormsModule, ReactiveFormsModule, QuillEditorComponent, RouterLink, CategorySelectComponent]
})
export class PostCreateComponent implements OnInit {
  postForm!: FormGroup;
  submitting = false;
  errorMessage: string | null = null;

  communityId!: number;
  profileId!: number;

  private readonly theme = injectCommunityTheme();
  private readonly categoryService = inject(PostCategoryService);

  categories: PostCategoryDTO[] = [];
  categoryId: number | null = null;

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
  this.theme.useCommunity(this.communityId);
  this.categoryService.getByCommunity(this.communityId).subscribe((list) => (this.categories = list));
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
      communityId: this.communityId,
      categoryId: this.categoryId
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
