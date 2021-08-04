package com.microservice.sample.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class ApiCompositionImplTest {

	RestTemplate restTemaplte = RestTemplateMockSupport.mock();
	
	@Test
	void testNormal() {
		List<Role> roles = new ArrayList<>();
		Role role1 = new Role();
		role1.roleId = "role1";
		role1.roleName = "ロール１";
		roles.add(role1);
		Role role2 = new Role();
		role2.roleId = "role2";
		role2.roleName = "ロール２";
		roles.add(role2);
		Role role3 = new Role();
		role3.roleId = "role3";
		role3.roleName = "ロール３";
		roles.add(role3);
		
		RestTemplateMockSupport.mockExchange(uri("test1"), restTemaplte, roles);
		
		List<Group> groups = new ArrayList<>();
		Group group1 = new Group();
		group1.groupId = "group1";
		group1.groupName = "グループ１";
		groups.add(group1);
		Group group2 = new Group();
		group2.groupId = "group2";
		group2.groupName = "グループ２";
		groups.add(group2);
		Group group3 = new Group();
		group3.groupId = "group3";
		group3.groupName = "グループ３";
		groups.add(group3);
		
		RestTemplateMockSupport.mockExchange(uri("test2"), restTemaplte, groups);
		
		//このAPIコンポジションは基本的にDBにおけるINNER JOINのみサポートされているイメージ
		ApiCompositionImpl<BaseEntity, ResultEntity> api = new ApiCompositionImpl<>(restTemaplte);
		
		//ベースエンティティから結果エンティティへのマッピングを定義
		api.mapToResult(b -> {
			ResultEntity entity = new ResultEntity();
			entity.username = b.username;
			entity.roleId = b.roleId;
			entity.groupId = b.groupId;
			return entity;
		});
		
		//呼び出すAPIの定義１
		//結果エンティティクラスから送信するURLと結果を受けるクラスを定義
		api.apiCall(Role.class, l -> {
				return uri("test1");
			})
			//レスポンスと結果エンティティのマッピング方法を指定
			//ベースエンティティから外部キーを特定して作成
			.map().keyFromEntity(e -> new Key(e.roleId))
					//レスポンスから外部キーを特定して作成
					.keyFromResponse(r -> new Key(r.roleId))
					//特定したレスポンスからエンティティクラスにデータを設定する
					.mapToEntity((re, r) -> {
								re.roleName = r.roleName;
							});
		api.apiCall(Group.class, l -> {
				return uri("test2");
			})
			.map().keyFromEntity(e -> new Key(e.groupId))
					.keyFromResponse(r -> new Key(r.groupId))
					.mapToEntity((re, r) -> {
							re.groupName = r.groupName;
						});
		
		List<BaseEntity> bases = new ArrayList<>();
		
		BaseEntity entity1 = new BaseEntity();
		entity1.username = "田中";
		entity1.roleId = "role1";
		entity1.groupId = "group1";
		
		bases.add(entity1);

		BaseEntity entity2 = new BaseEntity();
		entity2.username = "山田";
		entity2.roleId = "role3";
		entity2.groupId = "group2";
		
		bases.add(entity2);
		
		 List<ResultEntity> result = api.execute(bases);
		 
		 assertThat(result.size()).isEqualTo(2);
		 
		 ResultEntity result1 = result.get(0);
		 assertThat(result1.username).isEqualTo("田中");
		 assertThat(result1.roleId).isEqualTo("role1");
		 assertThat(result1.roleName).isEqualTo("ロール１");
		 assertThat(result1.groupId).isEqualTo("group1");
		 assertThat(result1.groupName).isEqualTo("グループ１");
		 
		 ResultEntity result2 = result.get(1);
		 assertThat(result2.username).isEqualTo("山田");
		 assertThat(result2.roleId).isEqualTo("role3");
		 assertThat(result2.roleName).isEqualTo("ロール３");
		 assertThat(result2.groupId).isEqualTo("group2");
		 assertThat(result2.groupName).isEqualTo("グループ２");
	}
	
	class BaseEntity {
		
		public String username;
		
		public String roleId;
		
		public String groupId;
	}
	
	class Role {
		
		public String roleId;
		
		public String roleName;
		
	}
	
	class Group {
		
		public String groupId;
		
		public String groupName;
	}
	
	class ResultEntity {
		
		public String username;
		
		public String roleId;
		
		public String roleName;
		
		public String groupId;
		
		public String groupName;

	}
	
	private URI uri(String uri) {
		URI r = null;
		try {
			r = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return r;
	}

}
