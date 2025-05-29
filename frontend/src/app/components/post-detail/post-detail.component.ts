import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PostService } from '../../services/post.service';
import { PostDetailDTO } from '../../models/post/PostDetailDTO';

@Component({
  selector: 'app-post-detail',
  standalone: false,
  templateUrl: './post-detail.component.html',
  styleUrl: './post-detail.component.css'
})
export class PostDetailComponent implements OnInit {
  postId!: number;
  post?: PostDetailDTO;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService
  ) {}

  ngOnInit(): void {
    this.postId = Number(this.route.snapshot.paramMap.get('id'));

    this.postService.getPostById(this.postId).subscribe({
      next: (data) => {
        console.log('POST RECIBIDO', data);
        console.log('¿Es array?', Array.isArray(data.postComment));
        this.post = data;
      },
      error: (err) => {
        console.error('Error al cargar el post', err);
      },
    });
  }
}
