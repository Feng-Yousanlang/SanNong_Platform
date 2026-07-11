import { DEFAULT_IMG, resolveImage } from '../api/utils';

function svgDataUri(svg: string): string {
  return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

const apple = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#f1f8e9" width="100%" height="100%"/><circle cx="200" cy="125" r="72" fill="#d32f2f"/><rect x="196" y="48" width="8" height="22" rx="4" fill="#558b2f"/></svg>',
);
const cabbage = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#f1f8e9" width="100%" height="100%"/><ellipse cx="200" cy="150" rx="90" ry="70" fill="#81c784"/><ellipse cx="175" cy="135" rx="35" ry="50" fill="#66bb6a"/><ellipse cx="225" cy="135" rx="35" ry="50" fill="#66bb6a"/></svg>',
);
const rice = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#fff8e1" width="100%" height="100%"/><ellipse cx="200" cy="165" rx="100" ry="55" fill="#fafafa"/><circle cx="160" cy="155" r="6" fill="#fff9c4"/><circle cx="200" cy="152" r="6" fill="#fff9c4"/><circle cx="240" cy="158" r="6" fill="#fff9c4"/></svg>',
);
const egg = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#fffde7" width="100%" height="100%"/><ellipse cx="170" cy="140" rx="32" ry="42" fill="#fff8e1"/><ellipse cx="230" cy="145" rx="32" ry="42" fill="#fff8e1"/><ellipse cx="200" cy="165" rx="32" ry="42" fill="#fff8e1"/></svg>',
);
const grape = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#f3e5f5" width="100%" height="100%"/><circle cx="200" cy="95" r="14" fill="#7b1fa2"/><circle cx="180" cy="115" r="14" fill="#8e24aa"/><circle cx="220" cy="115" r="14" fill="#8e24aa"/><circle cx="200" cy="130" r="14" fill="#7b1fa2"/><circle cx="165" cy="135" r="14" fill="#9c27b0"/><circle cx="235" cy="135" r="14" fill="#9c27b0"/><circle cx="200" cy="175" r="14" fill="#7b1fa2"/></svg>',
);
const potato = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#efebe9" width="100%" height="100%"/><ellipse cx="175" cy="145" rx="55" ry="38" fill="#a1887f"/><ellipse cx="225" cy="155" rx="50" ry="35" fill="#8d6e63"/><ellipse cx="200" cy="130" rx="45" ry="32" fill="#bcaaa4"/></svg>',
);

const SEED_PRODUCT_IMAGES: Record<string, string> = {
  有机红富士苹果: apple,
  新鲜小白菜: cabbage,
  '五常大米 5kg': rice,
  '散养土鸡蛋 30枚': egg,
  阳光玫瑰葡萄: grape,
  黄心土豆: potato,
};

const wheatNews = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="424" height="223"><rect fill="#e8f5e9" width="100%" height="100%"/><rect y="150" width="424" height="73" fill="#c5e1a5"/><circle cx="360" cy="45" r="28" fill="#fff59d"/></svg>',
);
const greenhouseNews = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="424" height="223"><rect fill="#e0f2f1" width="100%" height="100%"/><rect x="60" y="80" width="304" height="120" fill="#b2dfdb"/><polygon points="60,80 212,20 364,80" fill="#80cbc4"/></svg>',
);
const farmNews = svgDataUri(
  '<svg xmlns="http://www.w3.org/2000/svg" width="424" height="223"><rect fill="#fff3e0" width="100%" height="100%"/><path d="M0,130 Q106,60 212,130 Q318,60 424,130 L424,223 L0,223 Z" fill="#66bb6a"/></svg>',
);

const SEED_NEWS_IMAGES: Record<string, string> = {
  春季小麦田间管理要点: wheatNews,
  智慧大棚助力蔬菜稳产增收: greenhouseNews,
  农村电商带动农产品出村进城: farmNews,
};

export function productImageSrc(product: {
  productImg?: string;
  imageUrl?: string;
  productName?: string;
}): string {
  const name = product.productName?.trim();
  if (name && SEED_PRODUCT_IMAGES[name]) {
    return SEED_PRODUCT_IMAGES[name];
  }
  return resolveImage(product.productImg || product.imageUrl);
}

export function newsImageSrc(item: {
  imgUrl?: string;
  imageUrl?: string;
  title?: string;
}): string {
  const title = item.title?.trim();
  if (title && SEED_NEWS_IMAGES[title]) {
    return SEED_NEWS_IMAGES[title];
  }
  const resolved = resolveImage(item.imgUrl || item.imageUrl);
  return resolved === DEFAULT_IMG ? DEFAULT_IMG : resolved;
}
