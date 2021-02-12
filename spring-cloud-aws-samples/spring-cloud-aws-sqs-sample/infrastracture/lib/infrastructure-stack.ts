import * as cdk from '@aws-cdk/core';
import * as sqs from '@aws-cdk/aws-sqs';
import * as sns from '@aws-cdk/aws-sns';
import * as subs from '@aws-cdk/aws-sns-subscriptions';

export class InfrastructureStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

      new sqs.Queue(this, 'aws-pojo', { queueName: `${id}-aws-pojo` });


	  const topic = new sns.Topic(this, 'snsSpring', {
		  displayName: 'Spring cloud AWS SNS sample',
		  topicName: 'snsSpring',
	  });

	  const queue = new sqs.Queue(this, 'spring-aws', { queueName: `${id}-spring-aws` });

  }
}
