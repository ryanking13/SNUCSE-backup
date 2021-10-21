% Add common directory to your path for minfunc and mnist data helpers
addpath('./library/');
addpath(genpath('./library/minFunc_2012/minFunc'));
addpath('./functions/');

% load mnist data
[train,test] = load_mnist(false, true);
train.y = train.y+1; % make labels 1-based.
test.y = test.y+1; % make labels 1-based.

%==================== Model ====================%

% populate "ei" with the network architecture to train
% ei is a structure you can use to store hyperparameters of the network
% You can try different network architectures by changing ei
% only (no changes to the objective function code) later

% empty structure ...
ei = [];

% dimension of input features
ei.input_dim = 784;

% number of output classes
ei.output_dim = 10;

% sizes of all hidden layers... and the output layer
ei.layer_sizes = [256, ei.output_dim];

% scaling parameter for l2 weight regularization penalty
ei.lambda = 0.1;
% ei.lambda = 0;

% type of activation function to use in hidden layers
ei.activation_fun = 'sigmod';

% initial network as weights stack
stack = initialize_weights(ei);

% convert weight stack to the vector for minFunc
weight = stack2weight(stack);


%==================== Train ====================%

% setup minfunc options
options = [];
options.display = 'iter';
options.maxFunEvals = 1e5; % Maximum Function Call
options.optTol = 1e-5;     % Termination tolerance
options.Method = 'lbfgs';


%%%%%%%%%%%%%%%%%% TODO: Homework Question5 %%%%%%%%%%%%%%
%      Implement "mlp_cost.m" Function         %          
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


% Train mlp classifier using minFunc
numSamples = 60000;  %Try small training set (i.e. 3000) when you are doing homework.

[opt_weight, opt_value, exitflag, output] = minFunc(@mlp_cost,...
    weight, options, ei, train.X(:,1:numSamples), train.y(1:numSamples));


% compute accuracy on the train set
[~, ~, pred] = mlp_cost( opt_weight, ei, train.X, [], true);
[~,pred] = max(pred);
acc_train = mean(pred==train.y);
fprintf('train accuracy: %f\n', acc_train);


%==================== Test ====================%

% compute accuracy on the test set
[~, ~, pred] = mlp_cost( opt_weight, ei, test.X, [], true);
[~,pred] = max(pred);
acc_test = mean(pred==test.y);
fprintf('test accuracy: %f\n', acc_test);



% make sure your learned model get more than 95% test accuracy.
if acc_test > 0.97
    fprintf('good job!\n');
end


