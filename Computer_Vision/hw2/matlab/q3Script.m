[global1, direct1] = separateGlobalDirect('../data/q3/scene1');
[global2, direct2] = separateGlobalDirect('../data/q3/scene2');

figure(1);
imshow(global1);
title('Global Component 1');
% saveas(gcf, 'q3globalscene1.png');
figure(2);
imshow(direct1);
title('Direct Component 1');
% saveas(gcf, 'q3directscene1.png');

figure(3);
imshow(global2);
title('Global Component 2');
% saveas(gcf, 'q3globalscene2.png');
figure(4);
imshow(direct2);
title('Direct Component 2');
% saveas(gcf, 'q3directscene2.png');
